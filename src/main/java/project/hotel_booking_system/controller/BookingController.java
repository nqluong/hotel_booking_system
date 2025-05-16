package project.hotel_booking_system.controller;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.room_request.RoomSearchRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
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

    /**
     * Search for available rooms based on various criteria
     * 
     * @param searchRequest The search criteria including:
     *                     - checkInDate: Optional date to check in (nullable)
     *                     - checkOutDate: Optional date to check out (nullable)
     *                     - roomType: Optional room type filter (SINGLE, DOUBLE, SUITE) (nullable)
     *                     - minPrice: Optional minimum price filter (nullable)
     *                     - maxPrice: Optional maximum price filter (nullable)
     * @param page The page number (zero-based)
     * @param size The page size
     * @return Paginated list of available rooms matching the criteria
     * 
     * Example request body:
     * {
     *   "checkInDate": "2023-12-20",
     *   "checkOutDate": "2023-12-25",
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
}
