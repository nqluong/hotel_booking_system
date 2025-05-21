package project.hotel_booking_system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.service.BookingService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDTO createBooking(BookingCreationRequest request) {
        if (request.getCheckOutDate().before(request.getCheckInDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Date now = new Date();
        if (request.getCheckInDate().before(now)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(), 
                request.getCheckInDate(), 
                request.getCheckOutDate()
        );
        
        if (!overlappingBookings.isEmpty()) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        long durationInMillis = request.getCheckOutDate().getTime() - request.getCheckInDate().getTime();
        long days = TimeUnit.MILLISECONDS.toDays(durationInMillis);
        if (days < 1) days = 1;
        
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));
        

        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING) // Initial status is PENDING
                .totalPrice(totalPrice)
                .createdAt(LocalDateTime.now())
                .build();

        roomRepository.save(room);

        Booking savedBooking = bookingRepository.save(booking);
        
        return bookingMapper.toDTO(savedBooking);
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        return bookingMapper.toDTO(booking);
    }

    @Override
    public PaginationResponse<BookingResponseDTO> getUserBookings(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        
        // Find user bookings
        Page<Booking> bookingsPage = bookingRepository.findByUser_Id(userId, pageable);
        
        List<BookingResponseDTO> bookingResponses = bookingsPage.getContent()
                .stream()
                .map(bookingMapper::toDTO)
                .toList();
        
        return PaginationResponse.<BookingResponseDTO>builder()
                .content(bookingResponses)
                .page(bookingsPage.getNumber())
                .totalPages(bookingsPage.getTotalPages())
                .totalElements(bookingsPage.getTotalElements())
                .pageSize(bookingsPage.getSize())
                .build();
    }

    @Override
    public BookingResponseDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // Check if booking can be cancelled (not completed or already cancelled)
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(booking.getStatus() == BookingStatus.COMPLETED 
                    ? ErrorCode.COMPLETED_BOOKING_UPDATE 
                    : ErrorCode.CANCELLED_BOOKING_UPDATE);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Room room = booking.getRoom();
        room.setRoomStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        Booking updatedBooking = bookingRepository.save(booking);
        
        return bookingMapper.toDTO(updatedBooking);
    }
} 