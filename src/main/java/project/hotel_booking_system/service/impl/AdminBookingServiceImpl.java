package project.hotel_booking_system.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.service.AdminBookingService;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();

        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return bookingMapper.toDTO(booking);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdateDTO) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateStatusTransition(booking.getStatus(), statusUpdateDTO.getStatus());

        booking.setStatus(statusUpdateDTO.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);
        
        return bookingMapper.toDTO(updatedBooking);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }
    
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        // PENDING -> CONFIRMED -> CHECKED_IN -> COMPLETED
        
        switch (currentStatus) {
            case PENDING:
                if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                break;
            case CONFIRMED:
                if (newStatus != BookingStatus.CHECKED_IN && newStatus != BookingStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                break;
            case CHECKED_IN:
                if (newStatus != BookingStatus.COMPLETED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                break;
            case COMPLETED:
                throw new AppException(ErrorCode.COMPLETED_BOOKING_UPDATE);
            case CANCELLED:
                throw new AppException(ErrorCode.CANCELLED_BOOKING_UPDATE);
            default:
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

} 