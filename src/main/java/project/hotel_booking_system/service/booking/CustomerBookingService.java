package project.hotel_booking_system.service.booking;

import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerBookingService {
    BookingResponseDTO createBooking(BookingCreationRequest request);
    BookingResponseDTO getMyBooking(Long bookingId); // Removed userId parameter
    PaginationResponse<BookingResponseDTO> getMyBookings(Pageable pageable); // Removed userId parameter
    BookingResponseDTO cancelMyBooking(Long bookingId);
}
