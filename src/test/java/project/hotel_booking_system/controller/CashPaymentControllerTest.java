package project.hotel_booking_system.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.GlobalExceptionHandler;
import project.hotel_booking_system.service.CashPaymentService;
import project.hotel_booking_system.service.PaymentService;

@ExtendWith(MockitoExtension.class)
public class CashPaymentControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private CashPaymentService cashPaymentService;
    
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private CustomerPaymentController customerPaymentController;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerPaymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper.findAndRegisterModules();
    }
    
    @Test
    @DisplayName("Test process cash payment successfully")
    void testProcessCashPaymentSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        BigDecimal amount = new BigDecimal("500.00");
        Boolean staffConfirmation = true;
        
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(bookingId)
                .amount(amount)
                .staffConfirmation(staffConfirmation)
                .build();
        
        PaymentResponseDTO responseDTO = PaymentResponseDTO.builder()
                .id(1L)
                .bookingId(bookingId)
                .roomNumber("101")
                .userName("user1")
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .build();
        
        when(cashPaymentService.processCashPayment(any(CashPaymentRequestDTO.class)))
                .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/payments/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cash payment processed successfully"))
                .andExpect(jsonPath("$.result.bookingId").value(bookingId))
                .andExpect(jsonPath("$.result.amount").value(amount.doubleValue()))
                .andExpect(jsonPath("$.result.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.result.status").value("COMPLETED"));
    }
    
    @Test
    @DisplayName("Test process cash payment without staff confirmation")
    void testProcessCashPaymentWithoutStaffConfirmation() throws Exception {
        // Given
        Long bookingId = 1L;
        BigDecimal amount = new BigDecimal("500.00");
        Boolean staffConfirmation = false;
        
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(bookingId)
                .amount(amount)
                .staffConfirmation(staffConfirmation)
                .build();
        
        when(cashPaymentService.processCashPayment(any(CashPaymentRequestDTO.class)))
                .thenThrow(new AppException(ErrorCode.CASH_PAYMENT_REQUIRED));
        
        // When & Then
        mockMvc.perform(post("/payments/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cash payment confirmation is required"));
    }
    
    @Test
    @DisplayName("Test process cash payment with invalid amount")
    void testProcessCashPaymentWithInvalidAmount() throws Exception {
        // Given
        Long bookingId = 1L;
        BigDecimal amount = new BigDecimal("2000.00"); //
        Boolean staffConfirmation = true;
        
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(bookingId)
                .amount(amount)
                .staffConfirmation(staffConfirmation)
                .build();
        
        when(cashPaymentService.processCashPayment(any(CashPaymentRequestDTO.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT));
        
        // When & Then
        mockMvc.perform(post("/payments/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid payment amount"));
    }
    
    @Test
    @DisplayName("Test process cash payment with booking not found")
    void testProcessCashPaymentWithBookingNotFound() throws Exception {
        // Given
        Long bookingId = 999L; // Non-existent booking ID
        BigDecimal amount = new BigDecimal("500.00");
        Boolean staffConfirmation = true;
        
        CashPaymentRequestDTO requestDTO = CashPaymentRequestDTO.builder()
                .bookingId(bookingId)
                .amount(amount)
                .staffConfirmation(staffConfirmation)
                .build();
        
        when(cashPaymentService.processCashPayment(any(CashPaymentRequestDTO.class)))
                .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // When & Then
        mockMvc.perform(post("/payments/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }
    
    @Test
    @DisplayName("Test get remaining payment amount successfully")
    void testGetRemainingPaymentAmountSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        double remainingAmount = 350.00;
        
        when(cashPaymentService.getRemainingPaymentAmount(bookingId))
                .thenReturn(remainingAmount);
        
        // When & Then
        mockMvc.perform(get("/payments/cash/remaining/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Remaining payment amount retrieved successfully"))
                .andExpect(jsonPath("$.result").value(remainingAmount));
    }
    
    @Test
    @DisplayName("Test get remaining payment amount with booking not found")
    void testGetRemainingPaymentAmountWithBookingNotFound() throws Exception {
        // Given
        Long bookingId = 999L;
        
        when(cashPaymentService.getRemainingPaymentAmount(bookingId))
                .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // When & Then
        mockMvc.perform(get("/payments/cash/remaining/{bookingId}", bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }
} 