package project.hotel_booking_system.service.booking;

import java.util.List;

import org.springframework.data.domain.Pageable;

import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.enums.BookingStatus;

public interface BookingService {

    BookingResponseDTO createBooking(BookingCreationRequest bookingCreationRequest);

    BookingResponseDTO getBookingById(Long id);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO cancelBooking(Long bookingId);

    List<BookingResponseDTO> getBookingsByUser(Long id);

    PaginationResponse<BookingResponseDTO> getUserBookings(Long userId, Pageable pageable);

    BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdateDTO);

    List<BookingResponseDTO> getBookingsByStatus(BookingStatus status);
}
