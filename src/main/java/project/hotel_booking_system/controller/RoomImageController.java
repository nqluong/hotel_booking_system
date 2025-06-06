package project.hotel_booking_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.enums.ImageType;
import project.hotel_booking_system.service.room.RoomImageServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/room-images")
@Tag(name = "Room Image Management", description = "APIs for managing room images in hotel booking system")
@RequiredArgsConstructor
public class RoomImageController {

    @Autowired
    private RoomImageServiceImpl roomImageService;

    @PostMapping(value = "/upload/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload room image",
            description = "Upload a new image for a specific room. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format or missing parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<RoomImageResponse> uploadImage(
            @Parameter(description = "Room ID to upload image for", required = true)
            @PathVariable("roomId") Long roomId,

            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Type of image (THUMBNAIL, GALLERY, etc.)", required = true)
            @RequestParam("imageType") ImageType imageType) {
        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Image uploaded successfully")
                .result(roomImageService.uploadRoomImage(roomId,file,imageType))
                .build();


    }

    @GetMapping("/{roomId}")
    @Operation(
            summary = "Get all images by room ID",
            description = "Retrieve all images for a specific room. This endpoint is public and can be accessed by anyone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Images retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<List<RoomImageResponse>> getImagesByRoomNumber(
            @Parameter(description = "Room ID to get images for", required = true)
            @PathVariable("roomId") Long roomId) {
        return ApiResponseDTO.<List<RoomImageResponse>>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomImageService.getAllImagesByRoomNumber(roomId))
                .build();
    }

    @DeleteMapping("/{roomId}/images/{imageId}")
    @Operation(
            summary = "Delete room image",
            description = "Delete a specific image from a room. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room or image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<String> deleteRoomImage(
            @Parameter(description = "Room ID", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "Image ID to delete", required = true)
            @PathVariable Long imageId) {
        roomImageService.deleteImageByRoomNumber(roomId,imageId);
        return ApiResponseDTO.<String>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.NO_CONTENT.value())
                .message("Image deleted successfully")
                .result("Image deleted successfully")
                .build();
    }

    @PutMapping(value = "/{roomId}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update room image",
            description = "Replace an existing room image with a new one. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room or image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<RoomImageResponse> updateImage(
            @Parameter(description = "Room ID", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "Image ID to update", required = true)
            @PathVariable Long imageId,

            @Parameter(description = "New image file", required = true)
            @RequestParam("file") MultipartFile file) {
        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Image updated successfully")
                .result(roomImageService.updateRoomImage(roomId,imageId,file))
                .build();
    }

    @PutMapping("/{roomId}/images/{imageId}/type")
    @Operation(
            summary = "Update image type",
            description = "Change the type/category of an existing room image. Only administrators can perform this action.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image type updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid image type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Room or image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ApiResponseDTO<RoomImageResponse> updateImageType(
            @Parameter(description = "Room ID", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "Image ID to update", required = true)
            @PathVariable Long imageId,

            @Parameter(description = "New image type", required = true)
            @RequestParam("type") ImageType type) {
        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Image type updated successfully")
                .result(roomImageService.updateImageType(roomId,imageId,type))
                .build();
    }

}
