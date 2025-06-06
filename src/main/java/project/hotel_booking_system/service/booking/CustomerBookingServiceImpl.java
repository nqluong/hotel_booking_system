package project.hotel_booking_system.service.booking;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerBookingServiceImpl implements CustomerBookingService {

    BookingCoreService bookingCoreService;
    BookingRepository bookingRepository;
    RoomRepository roomRepository;
    UserRepository userRepository;
    BookingMapper bookingMapper;

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponseDTO createBooking(BookingCreationRequest request) {

        bookingCoreService.validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        bookingCoreService.validateRoomAvailability(request.getRoomId(),
                request.getCheckInDate(), request.getCheckOutDate());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        BigDecimal totalPrice = bookingCoreService.calculateTotalPrice(room,
                request.getCheckInDate(), request.getCheckOutDate());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostAuthorize("returnObject.userId == authentication.principal.id")
    public BookingResponseDTO getMyBooking(Long bookingId) {
        User currentUser = getCurrentUser();

        Booking booking = bookingCoreService.findBookingById(bookingId);

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return bookingMapper.toDTO(booking);
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaginationResponse<BookingResponseDTO> getMyBookings(Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Booking> bookingsPage = bookingRepository.findByUser_Id(currentUser.getId(), pageable);
        List<BookingResponseDTO> responses = bookingsPage.getContent()
                .stream()
                .map(bookingMapper::toDTO)
                .toList();

        return PaginationResponse.<BookingResponseDTO>builder()
                .content(responses)
                .page(bookingsPage.getNumber())
                .totalPages(bookingsPage.getTotalPages())
                .totalElements(bookingsPage.getTotalElements())
                .pageSize(bookingsPage.getSize())
                .build();
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponseDTO cancelMyBooking(Long bookingId) {
        User currentUser = getCurrentUser();


        Booking booking = bookingCoreService.findBookingById(bookingId);

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_BOOKING);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Room room = booking.getRoom();
        room.setRoomStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toDTO(updated);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
