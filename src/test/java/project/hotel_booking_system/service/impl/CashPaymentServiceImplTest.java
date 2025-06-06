package project.hotel_booking_system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.payment.CashPaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CashPaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @InjectMocks
    private CashPaymentServiceImpl cashPaymentService;
    
    private Booking testBooking;
    private User testUser;
    private Room testRoom;
    private Date checkInDate;
    private Date checkOutDate;

    private static final BigDecimal TOTAL_BOOKING_PRICE_VND = new BigDecimal("2500000.00");
    private static final BigDecimal EXISTING_PAYMENT_VND = new BigDecimal("800000.00");
    private static final BigDecimal CASH_PAYMENT_VND = new BigDecimal("1700000.00");
    private static final BigDecimal INVALID_PAYMENT_VND = new BigDecimal("2000000.00");

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .build();
        
       
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        checkInDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        checkOutDate = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .room(testRoom)
                .totalPrice(TOTAL_BOOKING_PRICE_VND)
                .status(BookingStatus.CHECKED_IN)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .build();
    }
    
    @Test
    @DisplayName("Test process cash payment successfully")
    void testProcessCashPaymentSuccessfully() {
        // Given
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(1L)
                .amount(CASH_PAYMENT_VND)
                .staffConfirmation(true)
                .build();

        Payment existingPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(EXISTING_PAYMENT_VND)
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment newPayment = Payment.builder()
                .id(2L)
                .booking(testBooking)
                .amount(CASH_PAYMENT_VND)
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .retryCount(0)
                .build();


        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(newPayment);
        when(paymentRepository.findByBookingId(1L))
                .thenReturn(Arrays.asList(existingPayment))
                .thenReturn(Arrays.asList(existingPayment, newPayment));

        // When
        PaymentResponseDTO result = cashPaymentService.processCashPayment(requestDTO);

        // Then
        assertEquals(2L, result.getId());
        assertEquals(1L, result.getBookingId());
        assertEquals("101", result.getRoomNumber());
        assertEquals("testuser", result.getUserName());
        assertEquals(CASH_PAYMENT_VND, result.getAmount());
        assertEquals(PaymentMethod.CASH, result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        // Verify booking status được update thành COMPLETED
        assertEquals(BookingStatus.COMPLETED, testBooking.getStatus());
        verify(bookingRepository, times(1)).save(testBooking);
        verify(paymentRepository, times(2)).findByBookingId(1L);
    }
    
    @Test
    @DisplayName("Test process cash payment without staff confirmation")
    void testProcessCashPaymentWithoutStaffConfirmation() {
        // Given
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(1L)
                .amount(CASH_PAYMENT_VND)
                .staffConfirmation(false)
                .build();
        
        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            cashPaymentService.processCashPayment(requestDTO);
        });
        
        assertEquals(ErrorCode.CASH_PAYMENT_REQUIRED, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("Test process cash payment with invalid amount")
    void testProcessCashPaymentWithInvalidAmount() {
        // Given
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(1L)
                .amount(INVALID_PAYMENT_VND)
                .staffConfirmation(true)
                .build();
        
        Payment existingPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(EXISTING_PAYMENT_VND)
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.COMPLETED)
                .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList(existingPayment));
        
        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            cashPaymentService.processCashPayment(requestDTO);
        });
        
        assertEquals(ErrorCode.INVALID_PAYMENT_AMOUNT, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("Test process cash payment with booking not found")
    void testProcessCashPaymentWithBookingNotFound() {
        // Given
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(999L)
                .amount(CASH_PAYMENT_VND)
                .staffConfirmation(true)
                .build();
        
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            cashPaymentService.processCashPayment(requestDTO);
        });
        
        assertEquals(ErrorCode.BOOKING_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("Test get remaining payment amount")
    void testGetRemainingPaymentAmount() {
        // Given
        Payment payment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(EXISTING_PAYMENT_VND)
                .status(PaymentStatus.COMPLETED)
                .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList(payment));
        
        // When
        double remainingAmount = cashPaymentService.getRemainingPaymentAmount(1L);
        
        // Then
        assertEquals(1700000.0, remainingAmount);
    }
    
    @Test
    @DisplayName("Test get remaining payment amount with no payments")
    void testGetRemainingPaymentAmountWithNoPayments() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Collections.emptyList());
        
        // When
        double remainingAmount = cashPaymentService.getRemainingPaymentAmount(1L);
        
        // Then
        assertEquals(2500000.0, remainingAmount);
    }
    
    @Test
    @DisplayName("Test get remaining payment amount with booking not found")
    void testGetRemainingPaymentAmountWithBookingNotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            cashPaymentService.getRemainingPaymentAmount(999L);
        });
        
        assertEquals(ErrorCode.BOOKING_NOT_FOUND, exception.getErrorCode());
    }
} 