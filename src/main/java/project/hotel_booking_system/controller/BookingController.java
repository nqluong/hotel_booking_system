package project.hotel_booking_system.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import project.hotel_booking_system.dto.request.room_request.RoomSearchRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.service.BookingService;
import project.hotel_booking_system.service.RoomService;

/**
 * Controller for handling booking-related operations for customers
 */
@RestController
@Slf4j
@RequestMapping("/bookings")
@Tag(name = "Booking API", description = "APIs for customer booking functionality")
public class BookingController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BookingService bookingService;

    /**
     * Example request body:
     * {
     *   "checkInDate": "2025-06-20",
     *   "checkOutDate": "2025-06-25",
     *   "roomType": "DOUBLE",
     *   "minPrice": 100.00,
     *   "maxPrice": 300.00
     * }
     */
    @Operation(
        summary = "Search for available rooms",
        description = "Search for available rooms based on date range, room type, and price range"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Rooms found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid search parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    @PostMapping("/available")
    public ApiResponseDTO<PaginationResponse<RoomResponse>> searchAvailableRooms(
            @Parameter(
                description = "Search criteria for finding available rooms",
                required = true,
                schema = @Schema(implementation = RoomSearchRequest.class)
            )
            @RequestBody RoomSearchRequest searchRequest,
            
            @Parameter(description = "Page number (0-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Searching for available rooms with criteria: {}", searchRequest);
        Pageable pageable = PageRequest.of(page, size);
        
        return ApiResponseDTO.<PaginationResponse<RoomResponse>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Available rooms retrieved successfully")
                .result(roomService.searchAvailableRooms(searchRequest, pageable))
                .build();
    }
    

     //Create a new booking

    @Operation(
        summary = "Create a new booking",
        description = "Create a new room booking with the specified dates and room"
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
        description = "Get detailed information about a specific booking"
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
        )
    })
    @GetMapping("/{id}")
    public ApiResponseDTO<BookingResponseDTO> getBookingById(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable("id") Long id) {
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking retrieved successfully")
                .result(bookingService.getBookingById(id))
                .build();
    }
    

     //Get all bookings for a specific user

    @Operation(
        summary = "Get user bookings",
        description = "Get all bookings for a specific user with pagination"
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
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    @GetMapping("/user/{userId}")
    public ApiResponseDTO<PaginationResponse<BookingResponseDTO>> getUserBookings(
            @Parameter(description = "User ID", required = true)
            @PathVariable("userId") Long userId,
            
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
                .result(bookingService.getUserBookings(userId, pageable))
                .build();
    }
    

     //Cancel a booking

    @Operation(
        summary = "Cancel booking",
        description = "Cancel an existing booking"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Booking cancelled successfully",
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
            responseCode = "400", 
            description = "Booking cannot be cancelled",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    @PutMapping("/{id}/cancel")
    public ApiResponseDTO<BookingResponseDTO> cancelBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable("id") Long id) {
        
        return ApiResponseDTO.<BookingResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Booking cancelled successfully")
                .result(bookingService.cancelBooking(id))
                .build();
    }
}
