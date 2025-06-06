package project.hotel_booking_system.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import project.hotel_booking_system.configuration.VnPayConfig;
import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.payment.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VnPayConfig vnPayConfig;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking testBooking;
    private User testUser;
    private Room testRoom;
    private Payment testPayment;
    private PaymentRequestDTO testPaymentRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomStatus(RoomStatus.AVAILABLE)
                .price(new BigDecimal("1000000"))
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .room(testRoom)
                .status(BookingStatus.PENDING)
                .totalPrice(new BigDecimal("2000000"))
                .createdAt(LocalDateTime.now())
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .booking(testBooking)
                .amount(new BigDecimal("600000"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.VNPAY)
                .retryCount(0)
                .build();

        testPaymentRequest = new PaymentRequestDTO();
        testPaymentRequest.setBookingId(1L);
        testPaymentRequest.setPaymentMethod(PaymentMethod.VNPAY);
        testPaymentRequest.setAdvancePayment(true);

        // Không setup VnPayConfig mock ở đây nữa, sẽ setup riêng cho các test cần
    }

    private void setupVnPayConfig() {
        when(vnPayConfig.getVersion()).thenReturn("2.1.0");
        when(vnPayConfig.getCommand()).thenReturn("pay");
        when(vnPayConfig.getTmnCode()).thenReturn("TMN123456");
        when(vnPayConfig.getCurrCode()).thenReturn("VND");
        when(vnPayConfig.getLocale()).thenReturn("vn");
        when(vnPayConfig.getOrderType()).thenReturn("billpayment");
        when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/hotelbooking/payments/vnpay-callback");
        when(vnPayConfig.getPaymentUrl()).thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        when(vnPayConfig.getHashSecret()).thenReturn("ABCDEFGHIJKLMNOPQRSTUVWXYZ123456");
    }

    @Test
    @DisplayName("Should get all payments")
    void shouldGetAllPayments() {
        // Given
        List<Payment> payments = Collections.singletonList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When
        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        assertEquals(testBooking.getId(), result.get(0).getBookingId());
    }

    @Test
    @DisplayName("Should get payment by ID")
    void shouldGetPaymentById() {
        // Given
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(testPayment));

        // When
        PaymentResponseDTO result = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        assertEquals(testBooking.getId(), result.getBookingId());
    }

    @Test
    @DisplayName("Should throw exception when payment not found by ID")
    void shouldThrowExceptionWhenPaymentNotFoundById() {
        // Given
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(1L));
    }

    @Test
    @DisplayName("Should get booking payments with pagination")
    void shouldGetBookingPaymentsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByBookingId(anyLong(), any(Pageable.class))).thenReturn(paymentPage);

        // When
        PaginationResponse<PaymentResponseDTO> result = paymentService.getBookingPayments(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should create new payment when it doesn't exist")
    void shouldCreateNewPayment() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(anyLong()))
                .thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponseDTO result = paymentService.createPayment(testPaymentRequest);

        // Then
        assertNotNull(result);
        assertEquals(testBooking.getId(), result.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should update existing payment when it already exists")
    void shouldUpdateExistingPayment() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(anyLong()))
                .thenReturn(Collections.singletonList(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponseDTO result = paymentService.createPayment(testPaymentRequest);

        // Then
        assertNotNull(result);
        assertEquals(testBooking.getId(), result.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process VNPay payment and generate URL")
    void shouldProcessVnPayPayment() {
        // Given
        setupVnPayConfig(); // Setup VnPayConfig mock chỉ cho test này
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(anyLong()))
                .thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponseDTO result = paymentService.processVnPayPayment(testPaymentRequest, "127.0.0.1");

        // Then
        assertNotNull(result);
        assertNotNull(result.getPaymentUrl());
        assertTrue(result.getPaymentUrl().startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
    }

    @Test
    @DisplayName("Should handle VNPay callback for successful payment")
    void shouldHandleVnPayCallbackForSuccessfulPayment() {
        // Given
        String callbackData = "vnp_ResponseCode=00&vnp_TxnRef=1_1234567890&vnp_Amount=60000000&" +
                "vnp_OrderInfo=Payment for booking with ID: 1&vnp_BankCode=NCB&vnp_TransactionNo=12345";

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        PaymentResponseDTO result = paymentService.handleVnPayCallback(callbackData);

        // Then
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should handle VNPay callback for failed payment")
    void shouldHandleVnPayCallbackForFailedPayment() {
        // Given
        String callbackData = "vnp_ResponseCode=24&vnp_TxnRef=1_1234567890&vnp_Amount=60000000&" +
                "vnp_OrderInfo=Payment for booking with ID: 1&vnp_BankCode=NCB&vnp_TransactionNo=12345";

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponseDTO result = paymentService.handleVnPayCallback(callbackData);

        // Then
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        // Should not update booking status for failed payment
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should extract payment ID from transaction reference with timestamp")
    void shouldExtractPaymentIdFromTransactionReferenceWithTimestamp() {
        // When
        try {
            // Call the private method using reflection
            java.lang.reflect.Method method = PaymentServiceImpl.class.getDeclaredMethod("extractPaymentId", String.class);
            method.setAccessible(true);
            Long result = (Long) method.invoke(paymentService, "1_1234567890");

            // Then
            assertEquals(1L, result);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should extract payment ID from simple transaction reference")
    void shouldExtractPaymentIdFromSimpleTransactionReference() {
        // When
        try {
            // Call the private method using reflection
            java.lang.reflect.Method method = PaymentServiceImpl.class.getDeclaredMethod("extractPaymentId", String.class);
            method.setAccessible(true);
            Long result = (Long) method.invoke(paymentService, "1");

            // Then
            assertEquals(1L, result);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid transaction reference format")
    void shouldThrowExceptionForInvalidTransactionReferenceFormat() {
        try {
            // Call the private method using reflection
            java.lang.reflect.Method method = PaymentServiceImpl.class.getDeclaredMethod("extractPaymentId", String.class);
            method.setAccessible(true);

            // When/Then
            assertThrows(InvocationTargetException.class, () -> {
                method.invoke(paymentService, "invalid_format");
            });
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}