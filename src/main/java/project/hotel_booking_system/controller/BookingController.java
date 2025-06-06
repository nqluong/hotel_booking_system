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
import project.hotel_booking_system.service.booking.CustomerBookingService;

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

    //Create a new booking

    @Operation(
            summary = "Create a new booking",
            description = "Create a new room booking with the specified dates and room",
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
            description = "Get detailed information about a specific booking belonging to current user",
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
            description = "Get all bookings for current authenticated user with pagination",
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

}
