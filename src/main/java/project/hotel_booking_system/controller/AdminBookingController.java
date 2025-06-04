package project.hotel_booking_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
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
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.service.AdminBookingService;
import project.hotel_booking_system.service.BookingService;
import project.hotel_booking_system.service.CashPaymentService;
import project.hotel_booking_system.service.CustomerBookingService;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin Booking Management", description = "Admin APIs for managing hotel reservations")
public class AdminBookingController {

    @Autowired
    private AdminBookingService adminBookingService;

    @Autowired
    private CashPaymentService cashPaymentService;

    @GetMapping
    @Operation(
            summary = "Get all bookings",
            description = "Retrieve a list of all bookings in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all bookings"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<List<BookingResponseDTO>> getAllBookings() {
        return ApiResponseDTO.<List<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Bookings retrieved successfully")
                .result(adminBookingService.getAllBookings())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking by ID",
            description = "Retrieve a specific booking by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the booking"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> getBookingById(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking retrieved successfully")
                .result(adminBookingService.getBookingById(id))
                .build();
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user bookings",
            description = "Retrieve all bookings for a specific user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user bookings"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<List<BookingResponseDTO>> getUserBookings(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        return ApiResponseDTO.<List<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User bookings retrieved successfully")
                .result(adminBookingService.getUserBookings(userId))
                .build();
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update booking status",
            description = "Update the status of a booking")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated the booking status"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> updateBookingStatus(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Status update details", required = true)
            @RequestBody BookingStatusUpdateDTO statusUpdateDTO) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking status updated successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdateDTO))
                .build();
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get bookings by status",
            description = "Retrieve all bookings with a specific status")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved bookings"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<List<BookingResponseDTO>> getBookingsByStatus(
            @Parameter(description = "Booking status", required = true)
            @PathVariable BookingStatus status) {
        return ApiResponseDTO.<List<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Bookings retrieved successfully")
                .result(adminBookingService.getBookingsByStatus(status))
                .build();
    }

    @PutMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm booking",
            description = "Update a booking status to CONFIRMED")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully confirmed the booking"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> confirmBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id) {

        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking confirmed successfully")
                .result(adminBookingService.confirmBooking(id))
                .build();
    }

    @PutMapping("/{id}/check-in")
    @Operation(
            summary = "Check-in guest",
            description = "Update a booking status to CHECKED_IN")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully checked in the guest"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> checkInBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Guest checked in successfully")
                .result(adminBookingService.checkInBooking(id))
                .build();
    }

    @PutMapping("/{id}/check-out")
    @Operation(
            summary = "Check-out guest",
            description = "Update a booking status to COMPLETED")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully checked out the guest"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition or incomplete payment",
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> checkOutBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id) {
        try {
            // Check if there's any remaining payment
            double remainingAmount = cashPaymentService.getRemainingPaymentAmount(id);

            if (remainingAmount > 0) {
                return ApiResponseDTO.<BookingResponseDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .time(LocalDateTime.now())
                        .message("Cannot complete checkout. Remaining payment amount: " + remainingAmount)
                        .success(false)
                        .build();
            }

            BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
            statusUpdate.setStatus(BookingStatus.COMPLETED);

            return ApiResponseDTO.<BookingResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .message("Guest checked out successfully")
                    .result(adminBookingService.updateBookingStatus(id, statusUpdate))
                    .build();
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.INCOMPLETE_PAYMENT) {
                return ApiResponseDTO.<BookingResponseDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .time(LocalDateTime.now())
                        .message("Cannot complete checkout. Payment is incomplete.")
                        .success(false)
                        .build();
            }
            throw ex;
        }
    }

    @PutMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel booking",
            description = "Update a booking status to CANCELLED"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully cancelled the booking"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    public ApiResponseDTO<BookingResponseDTO> cancelBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking cancelled successfully")
                .result(adminBookingService.cancelBooking(id))
                .build();
    }
} 