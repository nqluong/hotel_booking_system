package project.hotel_booking_system.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.PaymentMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CashPaymentServiceImpl implements CashPaymentService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    PaymentValidatorService paymentValidatorService;
    PaymentCalculatorService paymentCalculatorService;
    BookingStatusManager bookingStatusManager;
    PaymentMapper paymentMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponseDTO processCashPayment(CashPaymentRequestDTO cashPaymentRequestDTO) {

        Booking booking = bookingRepository.findById(cashPaymentRequestDTO.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        paymentValidatorService.validateCashPayment(cashPaymentRequestDTO, booking);

        Payment cashPayment = Payment.builder()
                .booking(booking)
                .amount(cashPaymentRequestDTO.getAmount())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .retryCount(0)
                .build();

        Payment savedPayment = paymentRepository.save(cashPayment);
        bookingStatusManager.updateBookingStatusAfterCashPayment(savedPayment);

        return paymentMapper.toDTO(savedPayment);

    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public double getRemainingPaymentAmount(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BigDecimal remainingAmount = paymentCalculatorService.calculateRemainingAmount(booking);
        return remainingAmount.doubleValue();
    }

} 