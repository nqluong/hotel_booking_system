package project.hotel_booking_system.service.payment;

import org.springframework.security.core.Authentication;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;

public interface BookingStatusManager {
    void updateBookingStatusAfterPayment(Payment payment);
    void updateBookingStatusAfterCashPayment(Payment payment);

}
