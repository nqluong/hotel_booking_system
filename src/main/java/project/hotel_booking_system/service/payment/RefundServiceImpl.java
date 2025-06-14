package project.hotel_booking_system.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.dto.request.payment_request.RefundRequestDTO;
import project.hotel_booking_system.dto.response.RefundEligibilityResponse;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.enums.RefundStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.RefundMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Refund;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.repository.RefundRepository;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.security.UserSecurity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundServiceImpl implements RefundService {

    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;
    RefundRepository refundRepository;
    UserRepository userRepository;
    VNPayGatewayService vnPayGatewayService;
    RefundMapper refundMapper;
    UserSecurity userSecurity;

    LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(14, 0);
    int REFUND_ELIGIBILITY_HOURS = 48;
    BigDecimal REFUND_PERCENTAGE = BigDecimal.valueOf(0.5);

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public RefundResponseDTO processBookingRefund(Long bookingId) {
        // Get current user from JWT
        User currentUser = getCurrentUser();

        Booking booking = validateBookingOwnership(bookingId, currentUser.getId());

        Payment vnpayPayment = findVnpayPayment(bookingId);

        if (vnpayPayment == null) {
            log.info("No completed VNPay payment found for booking ID: {}", bookingId);
            return null;
        }

        // Check refund eligibility
        if (!isRefundEligible(booking, vnpayPayment)) {
            throw new AppException(ErrorCode.REFUND_NOT_ELIGIBLE);
        }

        BigDecimal refundAmount = calculateRefundAmount(booking, vnpayPayment.getAmount());

        // Check for existing refund and create/update accordingly
        Refund refund = handleExistingRefund(bookingId, vnpayPayment, booking, refundAmount);

        RefundResponseDTO processedRefund = vnPayGatewayService.processVNPayRefund(refund);

        log.info("Refund processed successfully for booking ID: {}, user ID: {}, refund amount: {}",
                bookingId, currentUser.getId(), refundAmount);

        return processedRefund;
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public RefundEligibilityResponse checkRefundEligibility(Long bookingId) {
        User currentUser = getCurrentUser();

        Booking booking = validateBookingOwnership(bookingId, currentUser.getId());

        // Find VNPay payment
        Payment vnpayPayment = findVnpayPayment(bookingId);
        boolean hasVnpayPayment = vnpayPayment != null;

        long hoursUntilCheckIn = calculateHoursUntilCheckIn(booking);
        boolean eligible = hasVnpayPayment && isRefundEligible(booking, vnpayPayment);

        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal originalAmount = BigDecimal.ZERO;
        String reason = determineEligibilityReason(vnpayPayment, eligible, hoursUntilCheckIn);

        if (vnpayPayment != null) {
            originalAmount = vnpayPayment.getAmount();
            if (eligible) {
                refundAmount = calculateRefundAmount(booking, originalAmount);
            }
        }

        LocalDateTime checkInDateTime = getCheckInDateTime(booking);

        return RefundEligibilityResponse.builder()
                .eligible(eligible)
                .reason(reason)
                .refundAmount(refundAmount)
                .originalAmount(originalAmount)
                .hoursUntilCheckIn(hoursUntilCheckIn)
                .checkInDate(checkInDateTime)
                .hasVnpayPayment(hasVnpayPayment)
                .build();
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public RefundResponseDTO getRefundByBookingId(Long bookingId) {
        User currentUser = getCurrentUser();
        validateBookingOwnership(bookingId, currentUser.getId());

        Refund refund = refundRepository.findByBookingId(bookingId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No refund found for booking ID: " + bookingId));

        return refundMapper.toDTO(refund);
    }

    private Refund handleExistingRefund(Long bookingId, Payment payment, Booking booking, BigDecimal refundAmount) {
        Optional<Refund> existingRefund = findExistingRefund(bookingId);

        if (existingRefund.isPresent()) {
            Refund refund = existingRefund.get();

            // Check if refund is already completed
            if (refund.getStatus() == RefundStatus.COMPLETED) {
                throw new AppException(ErrorCode.REFUND_ALREADY_COMPLETED);
            }

            return updateExistingRefund(refund, refundAmount);
        } else {
            return createNewRefund(payment, booking, refundAmount);
        }
    }

    private Optional<Refund> findExistingRefund(Long bookingId) {
        return refundRepository.findByBookingId(bookingId)
                .stream()
                .findFirst();
    }

    private Refund updateExistingRefund(Refund existingRefund, BigDecimal newRefundAmount) {
        log.info("Updating existing refund ID: {} with new amount: {}",
                existingRefund.getId(), newRefundAmount);

        existingRefund.setRefundAmount(newRefundAmount);
        existingRefund.setStatus(RefundStatus.PENDING);
        existingRefund.setRefundReason("Customer re-initiated refund - more than 48 hours before check-in");
        existingRefund.setUpdatedAt(LocalDateTime.now());

        return refundRepository.save(existingRefund);
    }

    private Refund createNewRefund(Payment payment, Booking booking, BigDecimal refundAmount) {
        log.info("Creating new refund for booking ID: {} with amount: {}",
                booking.getId(), refundAmount);

        Refund refund = Refund.builder()
                .payment(payment)
                .booking(booking)
                .refundAmount(refundAmount)
                .status(RefundStatus.PENDING)
                .refundReason("Customer initiated refund - more than 48 hours before check-in")
                .createdAt(LocalDateTime.now())
                .build();

        return refundRepository.save(refund);
    }

    private String determineEligibilityReason(Payment vnpayPayment, boolean eligible, long hoursUntilCheckIn) {
        if (vnpayPayment == null) {
            return "No VNPay payment found for this booking";
        }

        if (eligible) {
            return "Eligible for 50% refund";
        }

        if (hoursUntilCheckIn < REFUND_ELIGIBILITY_HOURS) {
            return "Not eligible - less than 48 hours before check-in";
        }

        return "Not eligible - booking conditions not met";
    }


    private boolean isRefundEligible(Booking booking, Payment payment) {
        // Check payment method
        if (payment.getPaymentMethod() != PaymentMethod.VNPAY) {
            log.info("Payment method is not VNPay for booking ID: {}", booking.getId());
            return false;
        }

        // Check payment status
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            log.info("Payment is not completed for booking ID: {}", booking.getId());
            return false;
        }

        // Check time eligibility
        long hoursUntilCheckIn = calculateHoursUntilCheckIn(booking);
        boolean isEligible = hoursUntilCheckIn >= REFUND_ELIGIBILITY_HOURS;

        log.info("Booking ID: {}, Hours until check-in: {}, Refund eligible: {}",
                booking.getId(), hoursUntilCheckIn, isEligible);

        return isEligible;
    }

    private BigDecimal calculateRefundAmount(Booking booking, BigDecimal originalAmount) {
        long hoursUntilCheckIn = calculateHoursUntilCheckIn(booking);

        log.info("Booking ID: {}, Hours until check-in: {}", booking.getId(), hoursUntilCheckIn);

        if (hoursUntilCheckIn >= REFUND_ELIGIBILITY_HOURS) {
            BigDecimal refundAmount = originalAmount.multiply(REFUND_PERCENTAGE);
            log.info("Refund eligible: 50% of {}, refund amount: {}", originalAmount, refundAmount);
            return refundAmount;
        }

        return BigDecimal.ZERO;
    }

    private long calculateHoursUntilCheckIn(Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInDateTime = getCheckInDateTime(booking);
        return ChronoUnit.HOURS.between(now, checkInDateTime);
    }

    private LocalDateTime getCheckInDateTime(Booking booking) {
        LocalDate checkInLocalDate = booking.getCheckInDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return LocalDateTime.of(checkInLocalDate, STANDARD_CHECK_IN_TIME);
    }

    private Booking validateBookingOwnership(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access booking {} they don't own", userId, bookingId);
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return booking;
    }

    private Payment findVnpayPayment(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .stream()
                .filter(payment -> payment.getPaymentMethod() == PaymentMethod.VNPAY
                        && payment.getStatus() == PaymentStatus.COMPLETED)
                .findFirst()
                .orElse(null);
    }

    private User getCurrentUser() {
        Long userId = userSecurity.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
