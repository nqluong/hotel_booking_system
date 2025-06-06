package project.hotel_booking_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.service.PaymentService;

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(
            summary = "Get all payments",
            description = "Retrieve a list of all payments in the system",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all payments"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<PaymentResponseDTO>> getAllPayments() {
        return ApiResponseDTO.<List<PaymentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payments retrieved successfully")
                .result(paymentService.getAllPayments())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve a specific payment by its ID",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the payment"),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payment retrieved successfully")
                .result(paymentService.getPaymentById(id))
                .build();
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update payment status",
            description = "Update the status of a payment",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the payment status"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody PaymentStatusUpdateDTO statusUpdateDTO) {
        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payment status updated successfully")
                .result(paymentService.updatePaymentStatus(id, statusUpdateDTO))
                .build();
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get payments by status",
            description = "Retrieve all payments with a specific status",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payments"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByStatus(
            @PathVariable PaymentStatus status) {
        return ApiResponseDTO.<List<PaymentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payments retrieved successfully")
                .result(paymentService.getPaymentsByStatus(status))
                .build();
    }

    @PutMapping("/{id}/mark-as-completed")
    @Operation(
            summary = "Mark payment as completed",
            description = "Update payment status to COMPLETED",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked payment as completed"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<PaymentResponseDTO> markAsCompleted(@PathVariable Long id) {
        PaymentStatusUpdateDTO statusUpdate = new PaymentStatusUpdateDTO();
        statusUpdate.setStatus(PaymentStatus.COMPLETED);

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payment marked as completed successfully")
                .result(paymentService.updatePaymentStatus(id, statusUpdate))
                .build();
    }

    @PutMapping("/{id}/mark-as-failed")
    @Operation(
            summary = "Mark payment as failed",
            description = "Update payment status to FAILED",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked payment as failed"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<PaymentResponseDTO> markAsFailed(@PathVariable Long id) {
        PaymentStatusUpdateDTO statusUpdate = new PaymentStatusUpdateDTO();
        statusUpdate.setStatus(PaymentStatus.FAILED);

        return ApiResponseDTO.<PaymentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Payment marked as failed successfully")
                .result(paymentService.updatePaymentStatus(id, statusUpdate))
                .build();
    }
}
