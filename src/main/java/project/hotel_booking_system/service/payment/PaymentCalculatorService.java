package project.hotel_booking_system.service.payment;

import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;

import java.math.BigDecimal;

public interface PaymentCalculatorService {
    BigDecimal calculateAdvancePayment(BigDecimal totalPrice);

    BigDecimal calculateRemainingPayment(BigDecimal totalPrice);

    BigDecimal calculateRemainingAmount(Booking booking);

    BigDecimal getTotalPaidAmount(Long bookingId);

    BigDecimal calculatePaymentAmount(Booking booking, boolean isAdvancePayment, Payment existingPayment);
}
