package project.hotel_booking_system.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookingServiceImplTest {

    @Mock
    private BookingCoreService bookingCoreService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private AdminBookingServiceImpl adminBookingService;

    private User testUser;
    private Room testRoom;
    private Booking testBooking;
    private BookingResponseDTO testBookingResponse;

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
    }

    @Test
    void getAllBookings_Success() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        List<BookingResponseDTO> result = adminBookingService.getAllBookings();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getBookingById_Success() {
        when(bookingCoreService.getBookingById(1L)).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.getBookingById(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void getUserBookings_Success() {
        when(bookingRepository.findByUser_Id(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(testBooking)));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        List<BookingResponseDTO> result = adminBookingService.getUserBookings(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByStatus_Success() {
        when(bookingRepository.findByStatus(BookingStatus.PENDING))
                .thenReturn(Arrays.asList(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        List<BookingResponseDTO> result = adminBookingService.getBookingsByStatus(BookingStatus.PENDING);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void updateBookingStatus_Success() {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CONFIRMED);

        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.updateBookingStatus(1L, statusUpdate);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void confirmBooking_Success() {
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.confirmBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void checkInBooking_Success() {
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        doNothing().when(bookingCoreService).validateCheckInTime(any());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.checkInBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void checkOutBooking_Success() {
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        doNothing().when(bookingCoreService).validateFullPayment(any());
        doNothing().when(bookingCoreService).validateCheckOutTime(any());
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.checkOutBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void cancelBooking_Success() {
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponseDTO result = adminBookingService.cancelBooking(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
    }

    @Test
    void cancelBooking_CompletedBooking_ThrowsException() {
        testBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingCoreService.findBookingById(1L)).thenReturn(testBooking);

        assertThrows(AppException.class, () -> adminBookingService.cancelBooking(1L));
    }

    private Date getFutureDate(int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }
}
