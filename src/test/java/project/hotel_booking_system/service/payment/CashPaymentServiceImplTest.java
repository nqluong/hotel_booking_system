package project.hotel_booking_system.service.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.mapper.PaymentMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashPaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentValidatorService paymentValidatorService;

    @Mock
    private PaymentCalculatorService paymentCalculatorService;

    @Mock
    private BookingStatusManager bookingStatusManager;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private CashPaymentServiceImpl cashPaymentService;

    private Booking testBooking;
    private CashPaymentRequestDTO testPaymentRequest;
    private Payment testPayment;
    private PaymentResponseDTO testPaymentResponse;

    @BeforeEach
    void setUp() {
        testBooking = Booking.builder()
                .id(1L)
                .build();

        testPaymentRequest = new CashPaymentRequestDTO();
        testPaymentRequest.setBookingId(1L);
        testPaymentRequest.setAmount(new BigDecimal("1000000")); // 1,000,000 VND

        testPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(new BigDecimal("1000000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .build();

        testPaymentResponse = new PaymentResponseDTO();
        testPaymentResponse.setId(1L);
        testPaymentResponse.setAmount(new BigDecimal("1000000"));
    }

    @Test
    void processCashPayment_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);
        doNothing().when(paymentValidatorService).validateCashPayment(any(), any());
        doNothing().when(bookingStatusManager).updateBookingStatusAfterCashPayment(any());

        // Act
        PaymentResponseDTO result = cashPaymentService.processCashPayment(testPaymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentResponse.getId(), result.getId());
        assertEquals(testPaymentResponse.getAmount(), result.getAmount());

        verify(bookingRepository).findById(1L);
        verify(paymentValidatorService).validateCashPayment(any(), any());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingStatusManager).updateBookingStatusAfterCashPayment(any());
        verify(paymentMapper).toDTO(any(Payment.class));
    }

    @Test
    void processCashPayment_BookingNotFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () ->
            cashPaymentService.processCashPayment(testPaymentRequest)
        );

        verify(bookingRepository).findById(1L);
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void getRemainingPaymentAmount_Success() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("50.00");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentCalculatorService.calculateRemainingAmount(testBooking)).thenReturn(expectedAmount);

        // Act
        double result = cashPaymentService.getRemainingPaymentAmount(1L);

        // Assert
        assertEquals(expectedAmount.doubleValue(), result);
        verify(bookingRepository).findById(1L);
        verify(paymentCalculatorService).calculateRemainingAmount(testBooking);
    }

    @Test
    void getRemainingPaymentAmount_BookingNotFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () ->
            cashPaymentService.getRemainingPaymentAmount(1L)
        );

        verify(bookingRepository).findById(1L);
        verifyNoInteractions(paymentCalculatorService);
    }
}
