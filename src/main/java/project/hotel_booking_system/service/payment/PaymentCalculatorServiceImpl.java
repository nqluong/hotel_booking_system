package project.hotel_booking_system.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {

    PaymentRepository paymentRepository;

    @Override
    public BigDecimal calculateAdvancePayment(BigDecimal totalPrice) {
        return totalPrice.multiply(BigDecimal.valueOf(0.3));
    }

    @Override
    public BigDecimal calculateRemainingPayment(BigDecimal totalPrice) {
        return totalPrice.multiply(BigDecimal.valueOf(0.7));
    }

    @Override
    public BigDecimal calculateRemainingAmount(Booking booking) {
        BigDecimal totalPrice = booking.getTotalPrice();
        BigDecimal totalPaid = getTotalPaidAmount(booking.getId());
        return totalPrice.subtract(totalPaid);
    }

    @Override
    public BigDecimal getTotalPaidAmount(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        return payments.stream()
                .filter(payment -> PaymentStatus.COMPLETED.equals(payment.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculatePaymentAmount(Booking booking, boolean isAdvancePayment, Payment existingPayment) {
        if (isAdvancePayment) {
            return calculateAdvancePayment(booking.getTotalPrice());
        } else {
            if (existingPayment != null && PaymentStatus.COMPLETED.equals(existingPayment.getStatus())) {
                return calculateRemainingPayment(booking.getTotalPrice());
            }
        }
        return BigDecimal.ZERO;
    }
}
