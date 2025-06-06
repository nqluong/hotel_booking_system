package project.hotel_booking_system.service.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import project.hotel_booking_system.configuration.VnPayConfig;
import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.PaymentService;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    VnPayConfig vnPayConfig;
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponseDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(this::mapToPaymentResponseDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') ")
    public PaymentResponseDTO getPaymentById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        if (hasRole(auth, "CUSTOMER") && !payment.getBooking().getUser().getUsername().equals(currentUser)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return mapToPaymentResponseDTO(payment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusUpdateDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setStatus(statusUpdateDTO.getStatus());
        
        // If payment is completed, update booking status based on context
        if (PaymentStatus.COMPLETED.equals(statusUpdateDTO.getStatus())) {
            Booking booking = payment.getBooking();

            if (BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);
            }
        }
        
        return mapToPaymentResponseDTO(paymentRepository.save(payment));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(this::mapToPaymentResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO) {
        Booking booking = bookingRepository.findById(paymentRequestDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + paymentRequestDTO.getBookingId()));

        // Check if payment already exists for this booking
        Optional<Payment> existingPayment = paymentRepository.findByBookingId(paymentRequestDTO.getBookingId())
                .stream()
                .findFirst();

        BigDecimal amount = null;
        if (paymentRequestDTO.isAdvancePayment()) {
            // 30% advance payment
            amount = booking.getTotalPrice().multiply(BigDecimal.valueOf(0.3));
        }
        else {
            // Thanh toán khi trả phòng còn lại 70%
            if (existingPayment.isPresent() && PaymentStatus.COMPLETED.equals(existingPayment.get().getStatus())) {
                amount = booking.getTotalPrice().multiply(BigDecimal.valueOf(0.7));
            }
        }
        
        Payment payment;
        if (existingPayment.isPresent()) {
            // Update payment record
            payment = existingPayment.get();
            BigDecimal existingAmount = payment.getAmount();
            payment.setAmount(amount.add(existingAmount));
            payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
            payment.setStatus(PaymentStatus.PENDING);
            log.info("Updating existing payment record for booking ID: {}", paymentRequestDTO.getBookingId());
        } else {
            // Create new payment record
            payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod(paymentRequestDTO.getPaymentMethod())
                    .status(PaymentStatus.PENDING)
                    .retryCount(0)
                    .build();
            log.info("Creating new payment record for booking ID: {}", paymentRequestDTO.getBookingId());
        }
        
        return mapToPaymentResponseDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponseDTO processVnPayPayment(PaymentRequestDTO paymentRequestDTO, String clientIp) {
        // First create a payment record
        PaymentResponseDTO paymentResponse = createPayment(paymentRequestDTO);
        
        // Generate VNPAY payment URL
        String vnpayUrl = generateVnPayUrl(paymentResponse.getId(), paymentResponse.getAmount(), clientIp);
        paymentResponse.setPaymentUrl(vnpayUrl);
        
        return paymentResponse;
    }

    @Override
    @Transactional
    public PaymentResponseDTO handleVnPayCallback(String vnPayResponse) {
        Map<String, String> vnpParams = parseVnPayResponse(vnPayResponse);
        
        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        
        // Handling unique transaction code cases
        final Long paymentId = extractPaymentId(vnpTxnRef);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        Booking booking = payment.getBooking();
        
        // If payment is successful
        if ("00".equals(vnpResponseCode)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            if(payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            
            // Check booking status and payment amount to determine payment type
            if (BookingStatus.PENDING.equals(booking.getStatus())) {
                    //30% down payment upon booking
                if (payment.getAmount().compareTo(booking.getTotalPrice()) < 0) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                } else {
                    // 100% payment upfront
                    booking.setStatus(BookingStatus.CONFIRMED);
                }
            } else if (BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
                // If it is payment upon check-out, update the booking status to COMPLETED
                booking.setStatus(BookingStatus.COMPLETED);
            }
            
            bookingRepository.save(booking);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            // Increase payment attempts
            payment.setRetryCount(payment.getRetryCount() + 1);
            log.warn("Payment failed for booking ID: {}, retry count: {}", 
                    payment.getBooking().getId(), payment.getRetryCount());
        }

        if (vnpTxnRef.contains("_")) {
            removeTransactionMapping(vnpTxnRef);
        }
        
        return mapToPaymentResponseDTO(paymentRepository.save(payment));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public PaginationResponse<PaymentResponseDTO> getBookingPayments(Long bookingId, Pageable pageable) {
        Page<Payment> paymentsPage = paymentRepository.findByBookingId(bookingId, pageable);
        
        List<PaymentResponseDTO> paymentDTOs = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponseDTO)
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
    
    private PaymentResponseDTO mapToPaymentResponseDTO(Payment payment) {
        Booking booking = payment.getBooking();
        
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .bookingId(booking.getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .userName(booking.getUser().getUsername())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .build();
    }
    
    private String generateVnPayUrl(Long paymentId, BigDecimal amount, String clientIp) {
        if (clientIp == null || clientIp.isEmpty() || "0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }
        
        String vnp_Version = vnPayConfig.getVersion();
        String vnp_Command = vnPayConfig.getCommand();
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_CurrCode = vnPayConfig.getCurrCode();
        String vnp_Locale = vnPayConfig.getLocale();
        String vnp_OrderType = vnPayConfig.getOrderType();

        // Generate unique crypto transaction for each payment
        String uniqueTxnRef = paymentId + "_" + System.currentTimeMillis();
        
        String vnp_Amount = String.valueOf(amount.multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);

        // Use unique cryto transaction reference
        vnp_Params.put("vnp_TxnRef", uniqueTxnRef);

        vnp_Params.put("vnp_OrderInfo", "Payment for booking with ID: " + paymentId);

        vnp_Params.put("vnp_Locale", vnp_Locale);

        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        
        // Current time in VN timezone
        LocalDateTime vnpCreateDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = vnpCreateDate.format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_IpAddr", clientIp);
        
        // Sort params
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Error encoding VNPay parameters", e);
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        String fullUrl = vnPayConfig.getPaymentUrl() + "?" + query;
        
        // Save the relationship between uniqueTxnRef and payment ID for callback handling
        updateTransactionMapping(uniqueTxnRef, paymentId);
        
        return fullUrl;
    }
    
    // Map to temporarily store the relationship between the unique vnp_TxnRef and the payment ID
    private static final Map<String, Long> transactionMap = new HashMap<>();
    
    private void updateTransactionMapping(String txnRef, Long paymentId) {
        transactionMap.put(txnRef, paymentId);
        log.info("Added transaction mapping: {} -> {}", txnRef, paymentId);
    }
    
    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {

            return "";
        }
    }
    
    private Map<String, String> parseVnPayResponse(String vnPayResponse) {
        Map<String, String> vnpParams = new HashMap<>();
        
        String[] params = vnPayResponse.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                vnpParams.put(keyValue[0], keyValue[1]);
            }
        }
        
        return vnpParams;
    }
    
    private Long getPaymentIdFromTransactionMap(String txnRef) {
        Long paymentId = transactionMap.get(txnRef);
        if (paymentId != null) {
            log.info("Found payment ID {} for transaction reference {}", paymentId, txnRef);
        }
        return paymentId;
    }
    
    private void removeTransactionMapping(String txnRef) {
        if (transactionMap.containsKey(txnRef)) {
            transactionMap.remove(txnRef);
            log.info("Removed transaction mapping for: {}", txnRef);
        }
    }
    
    private Long extractPaymentId(String txnRef) {
    
        if (txnRef.contains("_")) {
            Long paymentId = getPaymentIdFromTransactionMap(txnRef);
            if (paymentId == null) {
                try {
                    paymentId = Long.parseLong(txnRef.split("_")[0]);
                    log.warn("Transaction mapping not found, extracted payment ID from transaction reference: {}", paymentId);
                    return paymentId;
                } catch (Exception e) {
                    log.error("Failed to parse payment ID from transaction reference: {}", txnRef, e);
                    throw new ResourceNotFoundException("Invalid transaction reference: " + txnRef);
                }
            }
            return paymentId;
        } else {
            try {
                return Long.parseLong(txnRef);
            } catch (NumberFormatException e) {
                log.error("Failed to parse payment ID: {}", txnRef, e);
                throw new ResourceNotFoundException("Invalid payment ID: " + txnRef);
            }
        }
    }

    private boolean hasRequiredRole(Authentication auth, String... roles) {
        return auth.getAuthorities().stream()
                .anyMatch(authority ->
                        Arrays.stream(roles)
                                .anyMatch(role -> authority.getAuthority().equals("ROLE_" + role)));
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
} 