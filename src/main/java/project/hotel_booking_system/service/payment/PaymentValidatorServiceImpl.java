package project.hotel_booking_system.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.service.payment.PaymentCalculatorService;
import project.hotel_booking_system.service.payment.PaymentValidatorService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentValidatorServiceImpl implements PaymentValidatorService {

    PaymentCalculatorService paymentCalculatorService;


    @Override
    public void validatePaymentAccess(Payment payment, Authentication auth) {
        String currentUser = auth.getName();
        if (hasRole(auth, "CUSTOMER") && !payment.getBooking().getUser().getUsername().equals(currentUser)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }


    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    @Override
    public void validateCashPayment(CashPaymentRequestDTO request, Booking booking) {
        if (!request.getStaffConfirmation()) {
            throw new AppException(ErrorCode.CASH_PAYMENT_REQUIRED);
        }

        BigDecimal remainingAmount = paymentCalculatorService.calculateRemainingAmount(booking);

        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }
}
