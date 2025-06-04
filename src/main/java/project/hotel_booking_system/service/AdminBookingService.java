package project.hotel_booking_system.service;


import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;

import java.util.List;

public interface AdminBookingService {

    List<BookingResponseDTO> getAllBookings();
    BookingResponseDTO getBookingById(Long id);
    List<BookingResponseDTO> getUserBookings(Long userId);
    List<BookingResponseDTO> getBookingsByStatus(BookingStatus status);
    BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdate);
    BookingResponseDTO confirmBooking(Long id);
    BookingResponseDTO checkInBooking(Long id);
    BookingResponseDTO checkOutBooking(Long id);
    BookingResponseDTO cancelBooking(Long id);
}
