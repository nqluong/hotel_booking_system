package project.hotel_booking_system.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerBookingServiceImplTest {

    @Mock
    private BookingCoreService bookingCoreService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CustomerBookingServiceImpl customerBookingService;

    private User testUser;
    private Room testRoom;
    private Booking testBooking;
    private BookingResponseDTO testBookingResponse;
    private BookingCreationRequest testBookingRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .roomStatus(RoomStatus.AVAILABLE)
                .price(new BigDecimal("1000000"))
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .room(testRoom)
                .status(BookingStatus.PENDING)
                .checkInDate(getFutureDate(1))
                .checkOutDate(getFutureDate(3))
                .totalPrice(new BigDecimal("2000000"))
                .build();

        testBookingResponse = BookingResponseDTO.builder()
                .id(1L)
                .userId(1L)
                .status(BookingStatus.PENDING)
                .build();

        testBookingRequest = new BookingCreationRequest();
        testBookingRequest.setUserId(1L);
        testBookingRequest.setRoomId(1L);
        testBookingRequest.setCheckInDate(getFutureDate(1));
        testBookingRequest.setCheckOutDate(getFutureDate(3));
    }

    @Test
    void createBooking_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);
        doNothing().when(bookingCoreService).validateBookingDates(any(), any());
        doNothing().when(bookingCoreService).validateRoomAvailability(any(), any(), any());
        when(bookingCoreService.calculateTotalPrice(any(), any(), any()))
                .thenReturn(new BigDecimal("2000000"));

        BookingResponseDTO result = customerBookingService.createBooking(testBookingRequest);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
        assertEquals(testBookingResponse.getStatus(), result.getStatus());
    }

    @Test
    void getMyBooking_Success() {
        setupSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        BookingResponseDTO result = customerBookingService.getMyBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
        assertEquals(testBookingResponse.getUserId(), result.getUserId());
    }

    @Test
    void getMyBookings_Success() {
        setupSecurityContext();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(testBooking));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findByUser_Id(1L, pageable)).thenReturn(bookingPage);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        PaginationResponse<BookingResponseDTO> result = customerBookingService.getMyBookings(pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void cancelMyBooking_Success() {
        setupSecurityContext();
        testBooking.setStatus(BookingStatus.PENDING);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = customerBookingService.cancelMyBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void cancelMyBooking_CompletedBooking_ThrowsException() {
        setupSecurityContext();
        testBooking.setStatus(BookingStatus.COMPLETED);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);

        assertThrows(AppException.class, () -> customerBookingService.cancelMyBooking(1L));
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
    }

    private Date getFutureDate(int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }
}
