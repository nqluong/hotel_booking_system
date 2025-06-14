package project.hotel_booking_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.RefundEligibilityResponse;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.service.payment.RefundService;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Refund Management", description = "APIs for managing booking refunds and cancellations")
@SecurityRequirement(name = "bearer-jwt")
public class RefundController {

    @Autowired
    RefundService refundService;

    /**
     * Check refund eligibility for a booking
     */
    @Operation(
            summary = "Check refund eligibility",
            description = "Check if a booking is eligible for refund based on cancellation policy and payment status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Eligibility check completed successfully",
                    content = @Content(schema = @Schema(implementation = RefundEligibilityResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - user doesn't own the booking"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            )
    })

    @GetMapping("/eligibility/{bookingId}")
    public ApiResponseDTO<RefundEligibilityResponse> checkRefundEligibility(
            @Parameter(description = "ID of the booking to check eligibility for", required = true)
            @PathVariable Long bookingId) {
        log.info("Checking refund eligibility for booking ID: {}", bookingId);

        try {
            RefundEligibilityResponse eligibility = refundService.checkRefundEligibility(bookingId);

            log.info("Refund eligibility checked for booking ID: {}, eligible: {}",
                    bookingId, eligibility.isEligible());

            return ApiResponseDTO.<RefundEligibilityResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message(eligibility.isEligible()
                           ? "Booking is eligible for refund"
                           : "Booking is not eligible for refund")
                    .result(eligibility)
                    .build();
        } catch (Exception e) {
            log.error("Error checking refund eligibility for booking ID: {}", bookingId, e);
            throw e;
        }
    }

    /**
     * Get refund status by booking ID
     */
    @Operation(
            summary = "Get refund status",
            description = "Retrieve the current refund status and details for a specific booking"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Refund status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RefundResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - user doesn't own the booking"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking or refund not found"
            )
    })
    @GetMapping("/status/{bookingId}")
    public ApiResponseDTO<RefundResponseDTO> getRefundStatus(
            @Parameter(description = "ID of the booking to get refund status for", required = true)
            @PathVariable Long bookingId) {
        log.info("Getting refund status for booking ID: {}", bookingId);

        try {
            RefundResponseDTO refundStatus = refundService.getRefundByBookingId(bookingId);

            log.info("Refund status retrieved for booking ID: {}, status: {}",
                    bookingId, refundStatus.getStatus());

            return ApiResponseDTO.<RefundResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Refund status retrieved successfully")
                    .result(refundStatus)
                    .build();
        } catch (Exception e) {
            log.error("Error getting refund status for booking ID: {}", bookingId, e);
            throw e;
        }
    }
}
