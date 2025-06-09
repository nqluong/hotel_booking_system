package project.hotel_booking_system.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCoreServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingCoreServiceImpl bookingCoreService;

    private Booking testBooking;
    private Room testRoom;
    private BookingResponseDTO testBookingResponse;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .roomStatus(RoomStatus.AVAILABLE)
                .price(new BigDecimal("1000000"))
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .status(BookingStatus.PENDING)
                .room(testRoom)
                .checkInDate(getFutureDate(1))
                .checkOutDate(getFutureDate(3))
                .totalPrice(new BigDecimal("2000000"))
                .build();

        testBookingResponse = BookingResponseDTO.builder()
                .id(1L)
                .status(BookingStatus.PENDING)
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(new BigDecimal("2000000"))
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingResponse);

        BookingResponseDTO result = bookingCoreService.getBookingById(1L);

        assertNotNull(result);
        assertEquals(testBookingResponse.getId(), result.getId());
        assertEquals(testBookingResponse.getStatus(), result.getStatus());
    }

    @Test
    void validateBookingDates_Success() {
        Date checkIn = getFutureDate(1);
        Date checkOut = getFutureDate(3);

        assertDoesNotThrow(() -> bookingCoreService.validateBookingDates(checkIn, checkOut));
    }

    @Test
    void validateBookingDates_InvalidRange() {
        Date checkIn = getFutureDate(3);
        Date checkOut = getFutureDate(1);

        assertThrows(AppException.class, () ->
            bookingCoreService.validateBookingDates(checkIn, checkOut)
        );
    }

    @Test
    void validateRoomAvailability_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findOverlappingBookings(any(), any(), any()))
            .thenReturn(Arrays.asList());

        assertDoesNotThrow(() ->
            bookingCoreService.validateRoomAvailability(1L, getFutureDate(1), getFutureDate(3))
        );
    }

    @Test
    void calculateTotalPrice_Success() {
        Date checkIn = getFutureDate(1);
        Date checkOut = getFutureDate(3);

        BigDecimal result = bookingCoreService.calculateTotalPrice(testRoom, checkIn, checkOut);

        assertEquals(new BigDecimal("2000000"), result);
    }

    @Test
    void validateFullPayment_Success() {
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList(testPayment));

        assertDoesNotThrow(() -> bookingCoreService.validateFullPayment(testBooking));
    }

    @Test
    void validateStatusTransition_Success() {
        assertDoesNotThrow(() ->
            bookingCoreService.validateStatusTransition(testBooking, BookingStatus.CONFIRMED)
        );
    }

    @Test
    void validateStatusTransition_InvalidTransition() {
        assertThrows(AppException.class, () ->
            bookingCoreService.validateStatusTransition(testBooking, BookingStatus.COMPLETED)
        );
    }

    private Date getFutureDate(int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }
}
