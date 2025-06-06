package project.hotel_booking_system.service.payment;

import org.springframework.security.core.Authentication;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;

public interface PaymentValidatorService {
    void validateCashPayment(CashPaymentRequestDTO request, Booking booking);
    void validatePaymentAccess(Payment payment, Authentication auth);
}
