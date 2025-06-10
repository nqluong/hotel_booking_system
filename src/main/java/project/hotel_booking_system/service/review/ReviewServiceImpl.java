package project.hotel_booking_system.service.review;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hotel_booking_system.dto.request.review.ReviewRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.ReviewResponse;
import project.hotel_booking_system.dto.response.ReviewSummaryResponse;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.ReviewMapper;
import project.hotel_booking_system.model.Review;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.ReviewRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.security.UserSecurity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {


    ReviewRepository reviewRepository;
    RoomRepository roomRepository;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    ReviewMapper reviewMapper;
    UserSecurity userSecurity;

    @Override
    @PreAuthorize("hasRole('CUSTOMER') and @userSecurity.isCurrentUser(#userId)")
    public ReviewResponse createReview( ReviewRequest request) {
        log.info("Creating review for user {} and room {}", request.getUserId(), request.getRoomId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        boolean hasCompletedBooking = bookingRepository.existsByUserIdAndRoomIdAndStatus(
                request.getUserId(), request.getRoomId(), BookingStatus.COMPLETED);

        if (!hasCompletedBooking) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        if (reviewRepository.existsByUserIdAndRoomId(request.getUserId(), request.getRoomId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .user(user)
                .room(room)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", savedReview.getId());

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER') and @userSecurity.isCurrentUser(#userId) and @userSecurity.isReviewOwner(#reviewId, @reviewRepository)")
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        log.info("Updating review {} by user {}", reviewId, request.getUserId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // Update review details
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Review {} updated successfully", reviewId);

        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER') and @userSecurity.isCurrentUser(#userId) and @userSecurity.isReviewOwner(#reviewId, @reviewRepository)")
    public void deleteReview(Long reviewId) {
        Long userId = userSecurity.getCurrentUserId();
        log.info("Deleting review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
        log.info("Review {} deleted successfully", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ReviewResponse> getReviewsByRoom(Long roomId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent()
                .stream()
                .map(reviewMapper::toResponse)
                .toList();

        return PaginationResponse.<ReviewResponse>builder()
                .content(reviewResponses)
                .page(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .pageSize(reviewPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER') and @userSecurity.isCurrentUser(#userId)")
    public PaginationResponse<ReviewResponse> getReviewsByUser(Pageable pageable) {

        Long userId = userSecurity.getCurrentUserId();

        Page <Review> reviewPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent()
                .stream()
                .map(reviewMapper::toResponse)
                .toList();

        return PaginationResponse.<ReviewResponse>builder()
                .content(reviewResponses)
                .page(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .pageSize(reviewPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getRoomReviewSummary(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Double avgRating = reviewRepository.findAverageRatingByRoomId(roomId).orElse(0.0);
        Long totalReviews = reviewRepository.countByRoomId(roomId);

        // Lấy phân bố rating
        List<Object[]> ratingDistribution = reviewRepository.findRatingDistributionByRoomId(roomId);
        Map<Byte, Long> ratingMap = new HashMap<>();

        for (Object[] row : ratingDistribution) {
            ratingMap.put((Byte) row[0], (Long) row[1]);
        }

        return ReviewSummaryResponse.builder()
                .roomId(roomId)
                .roomNumber(room.getRoomNumber())
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .fiveStars(ratingMap.getOrDefault((byte) 5, 0L))
                .fourStars(ratingMap.getOrDefault((byte) 4, 0L))
                .threeStars(ratingMap.getOrDefault((byte) 3, 0L))
                .twoStars(ratingMap.getOrDefault((byte) 2, 0L))
                .oneStar(ratingMap.getOrDefault((byte) 1, 0L))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent()
                .stream()
                .map(reviewMapper::toResponse)
                .toList();

        return PaginationResponse.<ReviewResponse>builder()
                .content(reviewResponses)
                .page(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .pageSize(reviewPage.getSize())
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
        log.info("Review {} deleted by admin", reviewId);
    }

}
