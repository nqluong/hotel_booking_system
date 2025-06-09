package project.hotel_booking_system.service.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.PaymentMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VNPayGatewayService vnPayGatewayService;

    @Mock
    private PaymentCalculatorService paymentCalculatorService;

    @Mock
    private PaymentValidatorService paymentValidatorService;

    @Mock
    private BookingStatusManager bookingStatusManager;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private PaymentResponseDTO testPaymentResponse;
    private Booking testBooking;
    private PaymentRequestDTO testPaymentRequest;

    @BeforeEach
    void setUp() {
        testBooking = Booking.builder()
                .id(1L)
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(new BigDecimal("2000000")) // 2,000,000 VND
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .build();

        testPaymentResponse = new PaymentResponseDTO();
        testPaymentResponse.setId(1L);
        testPaymentResponse.setAmount(new BigDecimal("2000000"));

        testPaymentRequest = new PaymentRequestDTO();
        testPaymentRequest.setBookingId(1L);
        testPaymentRequest.setPaymentMethod(PaymentMethod.VNPAY);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllPayments_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);

        // Act
        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPaymentResponse, result.get(0));
        verify(paymentRepository).findAll();
        verify(paymentMapper).toDTO(any(Payment.class));
    }

    @Test
    void getPaymentById_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentResponse);
        doNothing().when(paymentValidatorService).validatePaymentAccess(any(), any());

        // Act
        PaymentResponseDTO result = paymentService.getPaymentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentResponse, result);
        verify(paymentRepository).findById(1L);
        verify(paymentMapper).toDTO(testPayment);
        verify(paymentValidatorService).validatePaymentAccess(any(), any());
        verify(securityContext).getAuthentication();
    }

    @Test
    void getPaymentById_NotFound() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            paymentService.getPaymentById(1L)
        );
        verify(paymentRepository).findById(1L);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void updatePaymentStatus_Success() {
        // Arrange
        PaymentStatusUpdateDTO updateDTO = new PaymentStatusUpdateDTO();
        updateDTO.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);

        // Act
        PaymentResponseDTO result = paymentService.updatePaymentStatus(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentResponse, result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingStatusManager).updateBookingStatusAfterPayment(any(Payment.class));
    }

    @Test
    void createPayment_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Collections.emptyList());
        when(paymentCalculatorService.calculatePaymentAmount(any(), anyBoolean(), any()))
                .thenReturn(new BigDecimal("2000000"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);

        // Act
        PaymentResponseDTO result = paymentService.createPayment(testPaymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentResponse, result);
        verify(bookingRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDTO(any(Payment.class));

        // Verify the payment was created with correct values
        verify(paymentCalculatorService).calculatePaymentAmount(
            eq(testBooking),
            eq(testPaymentRequest.isAdvancePayment()),
            isNull()
        );
    }

    @Test
    void processVnPayPayment_Success() {
        // Arrange
        String clientIp = "127.0.0.1";
        String expectedUrl = "http://vnpay.test.url";

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Collections.emptyList());
        when(paymentCalculatorService.calculatePaymentAmount(any(), anyBoolean(), any()))
                .thenReturn(new BigDecimal("2000000"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);
        when(vnPayGatewayService.generatePaymentUrl(any(), any(), anyString())).thenReturn(expectedUrl);

        // Act
        PaymentResponseDTO result = paymentService.processVnPayPayment(testPaymentRequest, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result.getPaymentUrl());
        verify(vnPayGatewayService).generatePaymentUrl(any(), any(), eq(clientIp));
    }

    @Test
    void getBookingPayments_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Arrays.asList(testPayment);
        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByBookingId(1L, pageable)).thenReturn(paymentsPage);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(testPaymentResponse);

        // Act
        PaginationResponse<PaymentResponseDTO> result = paymentService.getBookingPayments(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testPaymentResponse, result.getContent().get(0));
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        verify(paymentRepository).findByBookingId(1L, pageable);
        verify(paymentMapper).toDTO(any(Payment.class));
    }
}
