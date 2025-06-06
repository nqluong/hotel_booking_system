package project.hotel_booking_system.controller;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.service.CashPaymentService;
import project.hotel_booking_system.service.PaymentService;

@RestController
@Slf4j
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Customer Payment API", description = "APIs for customer payment processing")
public class CustomerPaymentController {

    private final PaymentService paymentService;
    private final CashPaymentService cashPaymentService;

    @Operation(
            summary = "Process payment with VNPAY",
            description = "Process a payment for a booking using VNPAY payment gateway",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment process initiated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @PostMapping("/process-payment")
    public ApiResponseDTO<PaymentResponseDTO> processPayment(
            @Parameter(
                    description = "Payment details including booking ID and payment method",
                    required = true,
                    schema = @Schema(implementation = PaymentRequestDTO.class)
            )
            @RequestBody PaymentRequestDTO paymentRequestDTO,
            HttpServletRequest request) {

        log.info("Processing payment for booking ID: {}", paymentRequestDTO.getBookingId());
        String clientIp = getClientIp(request);

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Payment processing initiated successfully")
                .result(paymentService.processVnPayPayment(paymentRequestDTO, clientIp))
                .build();
    }

    @Operation(
            summary = "Process checkout payment",
            description = "Process the remaining payment (70%) during check-out",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Checkout payment process initiated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @PostMapping("/process-checkout-payment")
    public ApiResponseDTO<PaymentResponseDTO> processCheckoutPayment(
            @Parameter(
                    description = "Payment details for checkout including booking ID",
                    required = true,
                    schema = @Schema(implementation = PaymentRequestDTO.class)
            )
            @RequestBody PaymentRequestDTO paymentRequestDTO,
            HttpServletRequest request) {

        paymentRequestDTO.setAdvancePayment(false);

        String clientIp = getClientIp(request);

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Checkout payment processing initiated successfully")
                .result(paymentService.processVnPayPayment(paymentRequestDTO, clientIp))
                .build();
    }

    @Operation(
            summary = "Handle VNPAY payment callback",
            description = "Process the callback from VNPAY payment gateway"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment callback processed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid callback parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/vnpay-callback")
    public ApiResponseDTO<PaymentResponseDTO> handleVnPayCallback(
            @Parameter(description = "Full query string from VNPAY callback")
            @RequestParam String vnp_ResponseCode,
            @RequestParam String vnp_TxnRef,
            @RequestParam String vnp_Amount,
            @RequestParam String vnp_OrderInfo,
            @RequestParam String vnp_BankCode,
            @RequestParam String vnp_TransactionNo) {

        log.info("Received VNPAY callback for transaction: {}", vnp_TransactionNo);

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("vnp_ResponseCode=").append(vnp_ResponseCode).append("&");
        responseBuilder.append("vnp_TxnRef=").append(vnp_TxnRef).append("&");
        responseBuilder.append("vnp_Amount=").append(vnp_Amount).append("&");
        responseBuilder.append("vnp_OrderInfo=").append(vnp_OrderInfo).append("&");
        responseBuilder.append("vnp_BankCode=").append(vnp_BankCode).append("&");
        responseBuilder.append("vnp_TransactionNo=").append(vnp_TransactionNo);

        PaymentResponseDTO paymentResponse = paymentService.handleVnPayCallback(responseBuilder.toString());

        String message = "00".equals(vnp_ResponseCode)
                ? "Payment completed successfully"
                : "Payment failed";

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success("00".equals(vnp_ResponseCode))
                .message(message)
                .result(paymentResponse)
                .build();
    }

    @Operation(
            summary = "Get payment details",
            description = "Get detailed information about a specific payment",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ApiResponseDTO<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable("id") Long id) {

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Payment retrieved successfully")
                .result(paymentService.getPaymentById(id))
                .build();
    }

    @Operation(
            summary = "Get booking payments",
            description = "Get all payments for a specific booking with pagination",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/booking/{bookingId}")
    public ApiResponseDTO<PaginationResponse<PaymentResponseDTO>> getBookingPayments(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable("bookingId") Long bookingId,

            @Parameter(description = "Page number (0-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ApiResponseDTO.<PaginationResponse<PaymentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking payments retrieved successfully")
                .result(paymentService.getBookingPayments(bookingId, pageable))
                .build();
    }

    @Operation(
            summary = "Process cash payment",
            description = "Process a cash payment for a booking",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully processed cash payment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @PostMapping("/cash")
    public ApiResponseDTO<PaymentResponseDTO> processCashPayment(
            @Valid @RequestBody CashPaymentRequestDTO cashPaymentRequestDTO) {
        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Cash payment processed successfully")
                .result(cashPaymentService.processCashPayment(cashPaymentRequestDTO))
                .build();
    }

    @Operation(
            summary = "Get remaining payment amount",
            description = "Get the remaining amount to be paid for a booking",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved remaining amount",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/cash/remaining/{bookingId}")
    public ApiResponseDTO<Double> getRemainingPaymentAmount(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long bookingId) {
        return ApiResponseDTO.<Double>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Remaining payment amount retrieved successfully")
                .result(cashPaymentService.getRemainingPaymentAmount(bookingId))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }
} 