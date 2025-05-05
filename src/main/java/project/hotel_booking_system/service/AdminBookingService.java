package project.hotel_booking_system.service;

import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.enums.BookingStatus;

import java.util.List;

public interface AdminBookingService {

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO getBookingById(Long id);

    BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdateDTO);

    List<BookingResponseDTO> getBookingsByStatus(BookingStatus status);
} 