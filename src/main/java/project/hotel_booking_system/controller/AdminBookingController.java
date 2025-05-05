package project.hotel_booking_system.controller;

import java.time.LocalDateTime;
import java.util.List;

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
import project.hotel_booking_system.service.AdminBookingService;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin Booking Management", description = "Admin APIs for managing hotel reservations")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Retrieve a list of all bookings in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all bookings"),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<BookingResponseDTO>> getAllBookings() {
        return ApiResponseDTO.<List<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Bookings retrieved successfully")
                .result(adminBookingService.getAllBookings())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieve a specific booking by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the booking"),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Booking retrieved successfully")
                .result(adminBookingService.getBookingById(id))
                .build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the booking status"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody BookingStatusUpdateDTO statusUpdateDTO) {
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Booking status updated successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdateDTO))
                .build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status", description = "Retrieve all bookings with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bookings"),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<BookingResponseDTO>> getBookingsByStatus(
            @PathVariable BookingStatus status) {
        return ApiResponseDTO.<List<BookingResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Bookings retrieved successfully")
                .result(adminBookingService.getBookingsByStatus(status))
                .build();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm booking", description = "Update a booking status to CONFIRMED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully confirmed the booking"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> confirmBooking(@PathVariable Long id) {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CONFIRMED);
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Booking confirmed successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdate))
                .build();
    }

    @PutMapping("/{id}/check-in")
    @Operation(summary = "Check-in guest", description = "Update a booking status to CHECKED_IN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked in the guest"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> checkInBooking(@PathVariable Long id) {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CHECKED_IN);
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Guest checked in successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdate))
                .build();
    }

    @PutMapping("/{id}/check-out")
    @Operation(summary = "Check-out guest", description = "Update a booking status to COMPLETED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked out the guest"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> checkOutBooking(@PathVariable Long id) {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.COMPLETED);
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Guest checked out successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdate))
                .build();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Update a booking status to CANCELLED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully cancelled the booking"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<BookingResponseDTO> cancelBooking(@PathVariable Long id) {
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setStatus(BookingStatus.CANCELLED);
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Booking cancelled successfully")
                .result(adminBookingService.updateBookingStatus(id, statusUpdate))
                .build();
    }
} 