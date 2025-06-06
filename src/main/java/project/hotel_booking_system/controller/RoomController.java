package project.hotel_booking_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomSearchRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.service.RoomService;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/rooms")
@Tag(name = "Room Management", description = "APIs for managing hotel rooms and room availability")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(
            summary = "Create new room",
            description = "Create a new room in the hotel. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Room created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                                "time": "2025-05-15T10:30:00",
                                                "status": 201,
                                                "success": true,
                                                "message": "Room created successfully",
                                                "result": {
                                                    "id": 1,
                                                    "roomNumber": "101",
                                                    "roomType": "SINGLE",
                                                    "price": 500000.00,
                                                    "status": "AVAILABLE"
                                                }
                                            }
                                            """
                            )
                    )

            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or room already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "time": "2025-05-15T10:30:00",
                                                "status": 400,
                                                "success": false,
                                                "message": "Room number already exists"
                                            }
                                            """
                            )
                    )),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<RoomResponse> createRoom(
            @Parameter(description = "Room creation details", required = true,
                    example = """
                            {
                                "roomNumber": "101",
                                "roomType": "SINGLE",
                                "price": 550000.00,
                                "roomStatus": "AVAILABLE",
                                "description": "Comfortable single room with city view"
                            }
                            """)
            @Valid @RequestBody RoomCreationRequest request) {
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .message("Room created successfully")
                .result(roomService.createRoom(request))
                .build();
    }

    @GetMapping
    @Operation(
            summary = "Get all rooms ",
            description = "Retrieve all rooms with pagination. This endpoint is public and can be accessed by anyone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rooms retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "time": "2025-06-06T10:30:00",
                                "status": 200,
                                "success": true,
                                "result": {
                                    "content": [
                                        {
                                            "id": 1,
                                            "roomNumber": "101",
                                            "roomType": "SINGLE",
                                            "price": 1500000.00,
                                            "status": "AVAILABLE"
                                        }
                                    ],
                                    "totalElements": 50,
                                    "totalPages": 5,
                                    "currentPage": 0,
                                    "size": 10
                                }
                            }
                            """
                            ))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<PaginationResponse<RoomResponse>> getAllRoom(
            @Parameter(
                    description = "Page number (0-based)",
                    example = "0",
                    schema = @Schema(minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of items per page",
                    example = "10",
                    schema = @Schema(minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<RoomResponse> paginationResponse = roomService.getAllRoom(pageable);
        return ApiResponseDTO.<PaginationResponse<RoomResponse>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .result(paginationResponse)
                .build();
    }

    /**
     * Example request body:
     * {
     * "checkInDate": "2025-06-20",
     * "checkOutDate": "2025-06-25",
     * "roomType": "DOUBLE",
     * "minPrice": 1000000.00,
     * "maxPrice": 3000000.00
     * }
     */
    @PostMapping("/search")
    @Operation(
            summary = "Search for available rooms",
            description =  """
            Search for available rooms based on various criteria:
            - Date range (check-in and check-out dates)
            - Room type (SINGLE, DOUBLE, SUITE, etc.)
            - Price range (minimum and maximum price)
            
            All search parameters are optional. If no criteria are provided, all available rooms will be returned.
            """

    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rooms found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Search Results",
                                    value = """
                                {
                                    "time": "2025-06-06T10:30:00",
                                    "status": 200,
                                    "success": true,
                                    "message": "Available rooms retrieved successfully",
                                    "result": {
                                        "content": [
                                            {
                                                "id": 1,
                                                "roomNumber": "101",
                                                "roomType": "DOUBLE",
                                                "price": 2000000.00,
                                                "status": "AVAILABLE",
                                                "description": "Spacious double room with ocean view"
                                            }
                                        ],
                                        "totalElements": 15,
                                        "totalPages": 2,
                                        "currentPage": 0,
                                        "size": 10
                                    }
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters",
                    content = @Content(examples = @ExampleObject(
                            value = """
                    {
                        "time": "2025-06-06T10:30:00",
                        "status": 400,
                        "success": false,
                        "message": "Check-out date must be after check-in date"
                    }
                    """
                    ))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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


    @GetMapping("/{roomId}")
    @Operation(
            summary = "Get room by ID",
            description = "Retrieve detailed information about a specific room including images." +
                    "This endpoint is public and can be accessed by anyone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ApiResponseDTO<RoomResponse> getRoomById(
            @Parameter(description = "Room ID", required = true)
            @PathVariable("roomId") Long roomId) {
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Room details retrieved successfully")
                .result(roomService.getRoomByRoomNumber(roomId))
                .build();
    }

    @PutMapping("/{roomId}")
    @Operation(
            summary = "Update room",
            description = "Update room information such as price, status, or amenities. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ApiResponseDTO<RoomResponse> updateRoom(
            @Parameter(description = "Room ID", required = true)
            @PathVariable("roomId") Long roomId,

            @Parameter(description = "Room update details", required = true)
            @Valid @RequestBody RoomUpdateRequest request) {
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Room updated successfully")
                .result(roomService.updateRoom(roomId, request))
                .build();
    }

    @DeleteMapping("/{roomId}")
    @Operation(
            summary = "Delete room",
            description = "Delete a room from the system. Only administrators can perform this action. Note: This will permanently remove the room and all associated data.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete room with active bookings"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ApiResponseDTO<String> deleteRoom(
            @Parameter(description = "Room ID", required = true)
            @PathVariable("roomId") Long roomId) {
        roomService.deleteRoom(roomId);
        return ApiResponseDTO.<String>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.NO_CONTENT.value())
                .result("Room has been deleted successfully")
                .build();
    }

}
