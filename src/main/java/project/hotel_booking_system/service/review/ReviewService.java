package project.hotel_booking_system.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.hotel_booking_system.dto.request.review.ReviewRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.ReviewResponse;
import project.hotel_booking_system.dto.response.ReviewSummaryResponse;

public interface ReviewService {

    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse updateReview( Long reviewId, ReviewRequest request);

    void deleteReview(Long reviewId);

    PaginationResponse<ReviewResponse> getReviewsByRoom(Long roomId, Pageable pageable);

    PaginationResponse<ReviewResponse> getReviewsByUser(Pageable pageable);

    ReviewSummaryResponse getRoomReviewSummary(Long roomId);

    ReviewResponse getReview(Long reviewId);

    PaginationResponse<ReviewResponse> getAllReviews(Pageable pageable);

    void deleteReviewByAdmin(Long reviewId);
}
