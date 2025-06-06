package project.hotel_booking_system.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.service.payment.PaymentService;

@ExtendWith(MockitoExtension.class)
public class CustomerPaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CustomerPaymentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PaymentResponseDTO paymentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        paymentResponse = PaymentResponseDTO.builder()
                .id(1L)
                .bookingId(1L)
                .roomNumber("101")
                .userName("testuser")
                .amount(new BigDecimal("600000"))
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .paymentUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=60000000")
                .build();
    }

    @Test
    @DisplayName("Should process regular payment")
    void shouldProcessRegularPayment() throws Exception {
        // Given
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBookingId(1L);
        request.setPaymentMethod(PaymentMethod.VNPAY);
        request.setAdvancePayment(true);

        when(paymentService.processVnPayPayment(any(PaymentRequestDTO.class), anyString()))
                .thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(post("/payments/process-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment processing initiated successfully"))
                .andExpect(jsonPath("$.result.paymentUrl").value(paymentResponse.getPaymentUrl()));
    }
    
    @Test
    @DisplayName("Should process checkout payment")
    void shouldProcessCheckoutPayment() throws Exception {
        // Given
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBookingId(1L);
        request.setPaymentMethod(PaymentMethod.VNPAY);

        when(paymentService.processVnPayPayment(any(PaymentRequestDTO.class), anyString()))
                .thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(post("/payments/process-checkout-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Checkout payment processing initiated successfully"));
    }

    @Test
    @DisplayName("Should handle VNPay callback for successful payment")
    void shouldHandleVnPayCallbackForSuccessfulPayment() throws Exception {
        // Given
        PaymentResponseDTO successResponse = PaymentResponseDTO.builder()
                .id(1L)
                .status(PaymentStatus.COMPLETED)
                .build();
                
        when(paymentService.handleVnPayCallback(anyString())).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/payments/vnpay-callback")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "1_1234567890")
                        .param("vnp_Amount", "60000000")
                        .param("vnp_OrderInfo", "Payment for booking")
                        .param("vnp_BankCode", "NCB")
                        .param("vnp_TransactionNo", "13579"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment completed successfully"));
    }

    @Test
    @DisplayName("Should handle VNPay callback for failed payment")
    void shouldHandleVnPayCallbackForFailedPayment() throws Exception {
        // Given
        PaymentResponseDTO failedResponse = PaymentResponseDTO.builder()
                .id(1L)
                .status(PaymentStatus.FAILED)
                .build();
                
        when(paymentService.handleVnPayCallback(anyString())).thenReturn(failedResponse);

        // When & Then
        mockMvc.perform(get("/payments/vnpay-callback")
                        .param("vnp_ResponseCode", "24")
                        .param("vnp_TxnRef", "1_1234567890")
                        .param("vnp_Amount", "60000000")
                        .param("vnp_OrderInfo", "Payment for booking")
                        .param("vnp_BankCode", "NCB")
                        .param("vnp_TransactionNo", "13579"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }

    @Test
    @DisplayName("Should get payment by ID")
    void shouldGetPaymentById() throws Exception {
        // Given
        when(paymentService.getPaymentById(anyLong())).thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(get("/payments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.id").value(1));
    }

    @Test
    @DisplayName("Should get booking payments")
    void shouldGetBookingPayments() throws Exception {
        // Given
        List<PaymentResponseDTO> paymentList = new ArrayList<>();
        paymentList.add(paymentResponse);
        
        PaginationResponse<PaymentResponseDTO> paginationResponse = PaginationResponse.<PaymentResponseDTO>builder()
                .content(paymentList)
                .page(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();

        when(paymentService.getBookingPayments(eq(1L), any(Pageable.class))).thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/payments/booking/{bookingId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.content", hasSize(1)));
    }
} 