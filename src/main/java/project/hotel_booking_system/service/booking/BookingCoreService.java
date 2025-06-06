package project.hotel_booking_system.service.booking;

import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Room;

import java.math.BigDecimal;
import java.util.Date;

public interface BookingCoreService {
    BookingResponseDTO getBookingById(Long id);
    void validateBookingDates(Date checkIn, Date checkOut);
    void validateRoomAvailability(Long roomId, Date checkIn, Date checkOut);
    BigDecimal calculateTotalPrice(Room room, Date checkIn, Date checkOut);

    Booking findBookingById(Long id);
    void validateStatusTransition(Booking booking, BookingStatus newStatus);
    void validateFullPayment(Booking booking);
    void validateCheckInTime(Booking booking);
    void validateCheckOutTime(Booking booking);
}
