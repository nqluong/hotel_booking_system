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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.request.review.ReviewRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.ReviewResponse;
import project.hotel_booking_system.dto.response.ReviewSummaryResponse;
import project.hotel_booking_system.service.review.ReviewService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for managing hotel room reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @Operation(
            summary = "Create a new review",
            description = "Create a review for a room. User must have completed a booking for the room to leave a review. " +
                    "Each user can only review a room once. Requires CUSTOMER role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                    {
                        "code": 201,
                        "message": "Review created successfully",
                        "result": {
                            "id": 1,
                            "userId": 123,
                            "userFullname": "John Doe",
                            "roomId": 1,
                            "roomNumber": "101",
                            "rating": 5,
                            "comment": "Excellent service and clean room!",
                            "createdAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "User has not completed booking or already reviewed"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ApiResponseDTO<ReviewResponse> createReview(
            @Parameter(description = "Review details", required = true)
            @Valid @RequestBody ReviewRequest request) {

        return ApiResponseDTO.<ReviewResponse>builder()
                .status(HttpStatus.CREATED.value())
                .time(LocalDateTime.now())
                .message("Review created successfully")
                .result(reviewService.createReview(request))
                .build();
    }

    @PutMapping("/{reviewId}")
    @Operation(
            summary = "Update an existing review",
            description = "Update a review. Only the user who created the review can update it. Requires CUSTOMER role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this review"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ApiResponseDTO<ReviewResponse> updateReview(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "Updated review details", required = true)
            @Valid @RequestBody ReviewRequest request
            ) {

        return ApiResponseDTO.<ReviewResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Review updated successfully")
                .result(reviewService.updateReview(reviewId, request))
                .build();
    }

    @DeleteMapping("/update/{reviewId}")
    @Operation(
            summary = "Delete a review",
            description = "Delete a review. Only the user who created the review can delete it. Requires CUSTOMER role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this review"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ApiResponseDTO<Void> deleteReview(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long reviewId) {

        reviewService.deleteReview(reviewId);

        return ApiResponseDTO.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Review deleted successfully")
                .build();
    }

    @GetMapping("/room/{roomId}")
    @Operation(
            summary = "Get reviews for a room",
            description = "Retrieve all reviews for a specific room with pagination. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ApiResponseDTO<PaginationResponse<ReviewResponse>> getReviewsByRoom(
            @Parameter(description = "Room ID", required = true, example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<ReviewResponse> reviews = reviewService.getReviewsByRoom(roomId, pageable);

        return ApiResponseDTO.<PaginationResponse<ReviewResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Reviews retrieved successfully")
                .result(reviews)
                .build();
    }

    @GetMapping("/my-reviews")
    @Operation(
            summary = "Get current user's reviews",
            description = "Retrieve all reviews created by the authenticated user with pagination. Requires CUSTOMER role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ApiResponseDTO<PaginationResponse<ReviewResponse>> getMyReviews(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<ReviewResponse> reviews = reviewService.getReviewsByUser(pageable);

        return ApiResponseDTO.<PaginationResponse<ReviewResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Reviews retrieved successfully")
                .result(reviews)
                .build();
    }

    @GetMapping("/room/{roomId}/summary")
    @Operation(
            summary = "Get room review summary",
            description = "Get statistical summary of reviews for a room including average rating and rating distribution. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review summary retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Summary Response",
                                    value = """
                    {
                        "code": 200,
                        "message": "Review summary retrieved successfully",
                        "result": {
                            "roomId": 1,
                            "roomNumber": "101",
                            "averageRating": 4.5,
                            "totalReviews": 25,
                            "fiveStars": 15,
                            "fourStars": 8,
                            "threeStars": 2,
                            "twoStars": 0,
                            "oneStar": 0
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ApiResponseDTO<ReviewSummaryResponse> getRoomReviewSummary(
            @Parameter(description = "Room ID", required = true, example = "1")
            @PathVariable Long roomId) {

        ReviewSummaryResponse summary = reviewService.getRoomReviewSummary(roomId);

        return ApiResponseDTO.<ReviewSummaryResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Review summary retrieved successfully")
                .result(summary)
                .build();
    }

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "Get review by ID",
            description = "Retrieve detailed information about a specific review. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ApiResponseDTO<ReviewResponse> getReview(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long reviewId) {

        ReviewResponse review = reviewService.getReview(reviewId);

        return ApiResponseDTO.<ReviewResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Review retrieved successfully")
                .result(review)
                .build();
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @Operation(
            summary = "Get all reviews (Admin only)",
            description = "Retrieve all reviews in the system with pagination. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All reviews retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ApiResponseDTO<PaginationResponse<ReviewResponse>> getAllReviews(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PaginationResponse<ReviewResponse> reviews = reviewService.getAllReviews(pageable);

        return ApiResponseDTO.<PaginationResponse<ReviewResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("All reviews retrieved successfully")
                .result(reviews)
                .build();
    }

    @DeleteMapping("/admin/{reviewId}")
    @Operation(
            summary = "Delete any review (Admin only)",
            description = "Delete any review in the system. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ApiResponseDTO<Void> deleteReviewByAdmin(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long reviewId) {

        reviewService.deleteReviewByAdmin(reviewId);

        return ApiResponseDTO.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Review deleted successfully")
                .build();
    }
}