package project.hotel_booking_system.controller;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.booking_request.BookingCreationRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.enums.RefundStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.service.booking.CustomerBookingService;
import project.hotel_booking_system.service.payment.RefundService;

/**
 * Controller for handling booking-related operations for customers
 */
@RestController
@Slf4j
@RequestMapping("/bookings")
@Tag(name = "Customer Booking API", description = "APIs for customer booking functionality")
public class BookingController {

    @Autowired
    private CustomerBookingService bookingService;

    @Autowired
    RefundService refundService;

    //Create a new booking

    @Operation(
            summary = "Create a new booking",
            description = "Creates a new booking for the authenticated user. " +
                    "Validates room availability for the specified dates, " +
                    "calculates total price, and reserves the room. " +
                    "The booking will be in PENDING status initially.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @PostMapping
    public ApiResponseDTO<BookingResponseDTO> createBooking(
            @Parameter(
                    description = "Booking details including room, user and dates",
                    required = true,
                    schema = @Schema(implementation = BookingCreationRequest.class)
            )
            @RequestBody BookingCreationRequest request) {

        log.info("Creating new booking: {}", request);

        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking created successfully")
                .result(bookingService.createBooking(request))
                .build();
    }

    //Get a specific booking by ID

    @Operation(
            summary = "Get booking details",
            description = "Retrieves detailed information about a specific booking. " +
                    "Users can only access their own bookings. " +
                    "Returns complete booking information including room details, dates, " +
                    "pricing, and current status.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking found successfully",
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - booking does not belong to current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found with the specified ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ApiResponseDTO<BookingResponseDTO> getMyBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable("id") Long id) {

        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking retrieved successfully")
                .result(bookingService.getMyBooking(id))
                .build();
    }


    //Get all bookings for a specific user

    @Operation(
            summary = "Get user bookings",
            description = "Retrieves all bookings belonging to the authenticated user. " +
                    "Results are paginated and sorted by creation date. " +
                    "Includes booking status, dates, room information, and pricing for each booking.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bookings retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required - JWT token missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    @GetMapping("/my")
    public ApiResponseDTO<PaginationResponse<BookingResponseDTO>> getMyBookings(
            @Parameter(description = "Page number (0-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ApiResponseDTO.<PaginationResponse<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User bookings retrieved successfully")
                .result(bookingService.getMyBookings(pageable))
                .build();
    }


    @PostMapping("/{bookingId}/cancel-with-refund")
    @Operation(
            summary = "Cancel booking with refund processing",
            description = "Cancels a booking and processes refund based on the hotel's cancellation policy. " +
                    "Refund amount depends on how far in advance the cancellation is made. " +
                    "The booking status will be updated to CANCELLED only after successful refund processing. " +
                    "If no refund is applicable, the booking is cancelled immediately.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking cancelled and refund processed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "202",
                    description = "Refund is being processed. Booking will be cancelled once refund is completed.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Booking cannot be cancelled (e.g., already cancelled, checked-in, or refund processing failed)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required - JWT token missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - The booking does not belong to the current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found with the specified ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during refund processing",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<RefundResponseDTO> cancelBookingWithRefund(
            @Parameter(
                    description = "Unique identifier of the booking to cancel and refund",
                    required = true
            )
            @PathVariable Long bookingId) {

        try {
            RefundResponseDTO refundResponse = refundService.processBookingRefund(bookingId);

            if (refundResponse != null) {
                if (refundResponse.getStatus() == RefundStatus.COMPLETED) {
                    bookingService.cancelMyBooking(bookingId);
                    return ApiResponseDTO.<RefundResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Booking cancelled and refund processed successfully")
                            .result(refundResponse)
                            .build();
                } else if (refundResponse.getStatus() == RefundStatus.PROCESSING) {
                    return ApiResponseDTO.<RefundResponseDTO>builder()
                            .status(HttpStatus.ACCEPTED.value())
                            .message("Refund is being processed. Booking will be cancelled once refund is completed.")
                            .result(refundResponse)
                            .build();
                } else {
                    // Refund failed
                    return ApiResponseDTO.<RefundResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Refund processing failed. Booking not cancelled.")
                            .result(refundResponse)
                            .build();
                }
            } else {
                // No refund applicable, just cancel booking
                bookingService.cancelMyBooking(bookingId);
                return ApiResponseDTO.<RefundResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Booking cancelled - no refund applicable")
                        .result(null)
                        .build();
            }

        } catch (AppException e) {
            log.error("Error processing refund for booking {}: {}", bookingId, e.getMessage());
            throw e; // Let global exception handler deal with it
        } catch (Exception e) {
            log.error("Unexpected error processing refund for booking {}", bookingId, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{bookingId}/refund-status")
    @Operation(
            summary = "Get refund status for a booking",
            description = "Retrieves the current refund status and details for a specific booking. " +
                    "Returns information about refund amount, processing status, " +
                    "transaction ID, and estimated completion time. " +
                    "Only shows refunds associated with the current user's bookings.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Refund status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required - JWT token missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - The booking does not belong to the current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No refund found for this booking (booking may not exist or no refund was processed)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<RefundResponseDTO> getRefundStatus(
            @Parameter(
                    description = "Unique identifier of the booking to check refund status for",
                    required = true
            )
            @PathVariable Long bookingId) {
        try {
            RefundResponseDTO refundResponse = refundService.getRefundByBookingId(bookingId);
            return ApiResponseDTO.<RefundResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Refund status retrieved successfully")
                    .result(refundResponse)
                    .build();
        } catch (ResourceNotFoundException e) {
            return ApiResponseDTO.<RefundResponseDTO>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("No refund found for this booking")
                    .result(null)
                    .build();
        }
    }
}
