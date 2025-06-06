package project.hotel_booking_system.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingStatusManagerImpl implements BookingStatusManager {

    BookingRepository bookingRepository;
    PaymentCalculatorService paymentCalculatorService;

    @Override
    @Transactional
    public void updateBookingStatusAfterPayment(Payment payment) {
        Booking booking = payment.getBooking();

        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            if (BookingStatus.PENDING.equals(booking.getStatus())) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else if (BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
                booking.setStatus(BookingStatus.COMPLETED);
            }
            bookingRepository.save(booking);
        }
    }

    @Override
    @Transactional
    public void updateBookingStatusAfterCashPayment(Payment payment) {
        Booking booking = payment.getBooking();
        BigDecimal totalPaidAmount = paymentCalculatorService.getTotalPaidAmount(booking.getId());

        if (totalPaidAmount.compareTo(booking.getTotalPrice()) >= 0 &&
                BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        }
    }
}
