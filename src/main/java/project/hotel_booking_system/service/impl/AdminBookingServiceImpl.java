package project.hotel_booking_system.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.service.AdminBookingService;
import project.hotel_booking_system.service.BookingCoreService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBookingServiceImpl implements AdminBookingService {

    BookingCoreService bookingCoreService;
    BookingRepository bookingRepository;
    RoomRepository roomRepository;
    BookingMapper bookingMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO getBookingById(Long id) {
        return bookingCoreService.getBookingById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponseDTO> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUser_Id(userId, Pageable.unpaged()).getContent();
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponseDTO> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdate) {
        Booking booking = bookingCoreService.findBookingById(id);

        bookingCoreService.validateStatusTransition(booking, statusUpdate.getStatus());

        booking.setStatus(statusUpdate.getStatus());
        Booking updated = bookingRepository.save(booking);

        return bookingMapper.toDTO(updated);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO confirmBooking(Long id) {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CONFIRMED);
        return updateBookingStatus(id, statusUpdate);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO checkInBooking(Long id) {
        Booking booking =  bookingCoreService.findBookingById(id);
        bookingCoreService.validateCheckInTime(booking);

        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CHECKED_IN);
        return updateBookingStatus(id, statusUpdate);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO checkOutBooking(Long id) {
        Booking booking = bookingCoreService.findBookingById(id);

        bookingCoreService.validateFullPayment(booking);
        bookingCoreService.validateCheckOutTime(booking);

        Room room = booking.getRoom();
        room.setRoomStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.COMPLETED);
        return updateBookingStatus(id, statusUpdate);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponseDTO cancelBooking(Long id) {
        Booking booking = bookingCoreService.findBookingById(id);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.COMPLETED_BOOKING_UPDATE);
        }

        Room room = booking.getRoom();
        room.setRoomStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CANCELLED);
        return updateBookingStatus(id, statusUpdate);
    }

}
