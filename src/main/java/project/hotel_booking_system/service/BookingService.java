package project.hotel_booking_system.service;

import org.springframework.data.domain.Pageable;

import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;

public interface BookingService {

    BookingResponseDTO createBooking(BookingCreationRequest bookingCreationRequest);

    BookingResponseDTO getBookingById(Long id);

    PaginationResponse<BookingResponseDTO> getUserBookings(Long userId, Pageable pageable);

    BookingResponseDTO cancelBooking(Long id);
} 