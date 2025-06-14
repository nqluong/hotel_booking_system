package project.hotel_booking_system.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.PaymentMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    VNPayGatewayService vnPayGatewayService;
    PaymentCalculatorService paymentCalculatorService;
    PaymentValidatorService paymentValidatorService;
    BookingStatusManager bookingStatusManager;
    PaymentMapper paymentMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponseDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') ")
    public PaymentResponseDTO getPaymentById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        paymentValidatorService.validatePaymentAccess(payment, auth);

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusUpdateDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setStatus(statusUpdateDTO.getStatus());
        Payment savedPayment = paymentRepository.save(payment);

        bookingStatusManager.updateBookingStatusAfterPayment(savedPayment);

        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO) {
        Booking booking = bookingRepository.findById(paymentRequestDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + paymentRequestDTO.getBookingId()));

        Optional<Payment> existingPayment = paymentRepository.findByBookingId(paymentRequestDTO.getBookingId())
                .stream()
                .findFirst();

        BigDecimal amount = paymentCalculatorService.calculatePaymentAmount(
                booking,
                paymentRequestDTO.isAdvancePayment(),
                existingPayment.orElse(null)
        );

        Payment payment;
        if (existingPayment.isPresent()) {
            payment = existingPayment.get();
            BigDecimal existingAmount = payment.getAmount();
            payment.setAmount(amount.add(existingAmount));
            payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
            payment.setStatus(PaymentStatus.PENDING);
            log.info("Updating existing payment record for booking ID: {}", paymentRequestDTO.getBookingId());
        } else {
            payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod(paymentRequestDTO.getPaymentMethod())
                    .status(PaymentStatus.PENDING)
                    .retryCount(0)
                    .build();
            log.info("Creating new payment record for booking ID: {}", paymentRequestDTO.getBookingId());
        }

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponseDTO processVnPayPayment(PaymentRequestDTO paymentRequestDTO, String clientIp) {
        PaymentResponseDTO paymentResponse = createPayment(paymentRequestDTO);

        String vnpayUrl = vnPayGatewayService.generatePaymentUrl(
                paymentResponse.getId(),
                paymentResponse.getAmount(),
                clientIp
        );

        paymentResponse.setPaymentUrl(vnpayUrl);
        return paymentResponse;
    }

    @Override
    @Transactional
    public PaymentResponseDTO handleVnPayCallback(String vnPayResponse) {

        log.info("Handling VNPay callback with response: {}", vnPayResponse);
        Map<String, String> vnpParams = vnPayGatewayService.parseCallback(vnPayResponse);

        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        String transactionId = vnpParams.get("vnp_TransactionNo");
        String transactionDate = vnpParams.get("vnp_PayDate");
        LocalDateTime transactionDateTime = LocalDateTime.parse(transactionDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Long paymentId = vnPayGatewayService.extractPaymentId(vnpTxnRef);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        log.info("Update payment found for booking ID: {}", payment.getBooking().getId());
        if ("00".equals(vnpResponseCode)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(transactionId);
            if(payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setRetryCount(payment.getRetryCount() + 1);
            log.warn("Payment failed for booking ID: {}, retry count: {}",
                    payment.getBooking().getId(), payment.getRetryCount());
        }
        payment.setPaymentDate(transactionDateTime);
        Payment savedPayment = paymentRepository.save(payment);
        bookingStatusManager.updateBookingStatusAfterPayment(savedPayment);

        if (vnpTxnRef.contains("_")) {
            vnPayGatewayService.removeTransactionMapping(vnpTxnRef);
        }

        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public PaginationResponse<PaymentResponseDTO> getBookingPayments(Long bookingId, Pageable pageable) {
        Page<Payment> paymentsPage = paymentRepository.findByBookingId(bookingId, pageable);

        List<PaymentResponseDTO> paymentDTOs = paymentsPage.getContent().stream()
                .map(paymentMapper::toDTO)
                .toList();

        return PaginationResponse.<PaymentResponseDTO>builder()
                .content(paymentDTOs)
                .page(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .last(paymentsPage.isLast())
                .build();
    }
}
    


