package project.hotel_booking_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.request.room_request.BlockDatesRequest;
import project.hotel_booking_system.dto.response.*;
import project.hotel_booking_system.model.RoomBlockedDate;
import project.hotel_booking_system.service.room.RoomAvailabilityService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Room Availability Management", description = "APIs for managing room availability, blocking dates, and calendar operations")

public class RoomAvailabilityController {

    RoomAvailabilityService roomAvailabilityService;

    @Operation(
            summary = "Get room availability for date range",
            description = "Retrieves the availability status of a specific room within the given date range, " +
                    "including available dates, booked dates, and blocked dates"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room availability retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range (start date is after end date)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @GetMapping("/{roomId}/availability")
    public ApiResponseDTO<RoomAvailabilityResponse> getRoomAvailability(
            @Parameter(description = "Unique identifier of the room", required = true, example = "1")
            @PathVariable Long roomId,

            @Parameter(description = "Start date for availability check (inclusive)", required = true, example = "2024-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for availability check (inclusive)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        RoomAvailabilityResponse availability = roomAvailabilityService
                .getRoomAvailability(roomId, startDate, endDate);

        return ApiResponseDTO.<RoomAvailabilityResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Room availability retrieved successfully")
                .result(availability)
                .build();
    }

    @Operation(
            summary = "Get availability for all rooms",
            description = "Retrieves paginated availability information for all rooms within the specified date range. " +
                    "Supports sorting and pagination for efficient data retrieval"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All rooms availability retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range or pagination parameters",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @GetMapping("/availability")
    public ApiResponseDTO<PaginationResponse<RoomAvailabilityPageResponse>> getAllRoomsAvailability(
            @Parameter(description = "Start date for availability check", required = true, example = "2024-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for availability check", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "roomNumber")
            @RequestParam(defaultValue = "roomNumber") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginationResponse<RoomAvailabilityPageResponse> availability =
                roomAvailabilityService.getAllRoomsAvailability(startDate, endDate, pageable);

        return ApiResponseDTO.<PaginationResponse<RoomAvailabilityPageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("All rooms availability retrieved successfully")
                .result(availability)
                .build();
    }

    @Operation(
            summary = "Get paginated calendar view",
            description = "Retrieves a calendar view showing room availability for a specific month and year. " +
                    "Returns paginated daily information with room status for each day"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated calendar view retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid month (must be between 1-12) or year",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @GetMapping("/availability/calendar")
    public ApiResponseDTO<PaginationResponse<CalendarDayResponse>> getCalendarViewPaginated(
            @Parameter(description = "Year for calendar view", required = true, example = "2024")
            @RequestParam int year,

            @Parameter(description = "Month for calendar view (1-12)", required = true, example = "12")
            @RequestParam int month,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of days per page", example = "7")
            @RequestParam(defaultValue = "7") int size) {

        if (month < 1 || month > 12) {
            return ApiResponseDTO.<PaginationResponse<CalendarDayResponse>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid month. Month must be between 1 and 12")
                    .build();
        }

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<CalendarDayResponse> calendar =
                roomAvailabilityService.getCalendarViewPaginated(year, month, pageable);

        return ApiResponseDTO.<PaginationResponse<CalendarDayResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Paginated calendar view retrieved successfully")
                .result(calendar)
                .build();
    }

    @Operation(
            summary = "Get blocked dates for a room",
            description = "Retrieves paginated list of blocked dates for a specific room within the given date range. " +
                    "Blocked dates are periods when the room is unavailable for booking due to maintenance or other reasons"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Blocked dates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @GetMapping("/{roomId}/blocked-dates")
    public ApiResponseDTO<PaginationResponse<RoomBlockedDate>> getBlockedDates(
            @Parameter(description = "Unique identifier of the room", required = true, example = "1")
            @PathVariable Long roomId,

            @Parameter(description = "Start date for blocked dates search", required = true, example = "2024-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for blocked dates search", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<RoomBlockedDate> blockedDates =
                roomAvailabilityService.getBlockedDates(roomId, startDate, endDate, pageable);

        return ApiResponseDTO.<PaginationResponse<RoomBlockedDate>>builder()
                .status(HttpStatus.OK.value())
                .message("Blocked dates retrieved successfully")
                .result(blockedDates)
                .build();
    }

    @Operation(
            summary = "Block dates for a room",
            description = "Blocks specific dates for a room, making them unavailable for booking. " +
                    "This operation requires admin privileges and validates that dates are not in the past " +
                    "and not already booked by guests",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dates blocked successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid dates (past dates or already booked dates)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - admin privileges required",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Room already booked for specified dates",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @PutMapping("/{roomId}/block-dates")
    public ApiResponseDTO<Void> blockDates(
            @Parameter(description = "Unique identifier of the room", required = true, example = "1")
            @PathVariable Long roomId,

            @Parameter(description = "Request containing dates to block and reason", required = true)
            @Valid @RequestBody BlockDatesRequest request) {


        roomAvailabilityService.blockDates(roomId, request);

        return ApiResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Dates blocked successfully")
                .build();
    }

    @Operation(
            summary = "Unblock dates for a room",
            description = "Removes blocking from specific dates for a room, making them available for booking again. " +
                    "This operation requires admin privileges",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dates unblocked successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - admin privileges required",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            )
    })
    @DeleteMapping("/{roomId}/block-dates")
    public ApiResponseDTO<Void> unblockDates(
            @Parameter(description = "Unique identifier of the room", required = true, example = "1")
            @PathVariable Long roomId,

            @Parameter(description = "List of dates to unblock", required = true,
                    example = "2024-12-25,2024-12-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> dates) {

        roomAvailabilityService.unblockDates(roomId, dates);

        return ApiResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Dates unblocked successfully")
                .build();
    }

    /**
     * GET /rooms/{roomId}/availability/quick - Quick check availability for specific dates
     */
    @GetMapping("/{roomId}/availability/quick")
    public ApiResponseDTO<Boolean> quickAvailabilityCheck(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        RoomAvailabilityResponse availability = roomAvailabilityService
                .getRoomAvailability(roomId, checkIn, checkOut.minusDays(1));

        // Check if all dates between checkIn and checkOut-1 are available
        boolean isAvailable = checkIn.datesUntil(checkOut)
                .allMatch(date -> availability.getAvailableDates().contains(date));

        return ApiResponseDTO.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("Quick availability check completed")
                .result(isAvailable)
                .build();
    }
}
