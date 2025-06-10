package project.hotel_booking_system.service.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserSecurity userSecurity;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Room testRoom;
    private Review testReview;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .build();

        testReview = Review.builder()
                .id(1L)
                .user(testUser)
                .room(testRoom)
                .rating((byte) 5)
                .comment("Excellent room!")
                .createdAt(LocalDateTime.now())
                .build();

        reviewRequest = ReviewRequest.builder()
                .userId(1L)
                .roomId(1L)
                .rating((byte) 5)
                .comment("Excellent room!")
                .build();

        reviewResponse = ReviewResponse.builder()
                .id(1L)
                .userId(1L)
                .roomId(1L)
                .rating((byte) 5)
                .comment("Excellent room!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create review successfully when all conditions are met")
    void createReview_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.existsByUserIdAndRoomIdAndStatus(1L, 1L, BookingStatus.COMPLETED))
                .thenReturn(true);
        when(reviewRepository.existsByUserIdAndRoomId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.createReview(reviewRequest);

        // Then
        assertNotNull(result);
        assertEquals(reviewResponse.getId(), result.getId());
        assertEquals(reviewResponse.getRating(), result.getRating());
        assertEquals(reviewResponse.getComment(), result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createReview_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when room not found")
    void createReview_RoomNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when user has no completed booking")
    void createReview_NoCompletedBooking() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.existsByUserIdAndRoomIdAndStatus(1L, 1L, BookingStatus.COMPLETED))
                .thenReturn(false);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals(ErrorCode.REVIEW_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when review already exists")
    void createReview_ReviewAlreadyExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.existsByUserIdAndRoomIdAndStatus(1L, 1L, BookingStatus.COMPLETED))
                .thenReturn(true);
        when(reviewRepository.existsByUserIdAndRoomId(1L, 1L)).thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should update review successfully")
    void updateReview_Success() {
        // Given
        ReviewRequest updateRequest = ReviewRequest.builder()
                .userId(1L)
                .roomId(1L)
                .rating((byte) 4)
                .comment("Updated comment")
                .build();

        Review updatedReview = Review.builder()
                .id(1L)
                .user(testUser)
                .room(testRoom)
                .rating((byte) 4)
                .comment("Updated comment")
                .createdAt(LocalDateTime.now())
                .build();

        ReviewResponse updatedResponse = ReviewResponse.builder()
                .id(1L)
                .rating((byte) 4)
                .comment("Updated comment")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toResponse(updatedReview)).thenReturn(updatedResponse);

        // When
        ReviewResponse result = reviewService.updateReview(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals((byte) 4, result.getRating());
        assertEquals("Updated comment", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent review")
    void updateReview_ReviewNotFound() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.updateReview(1L, reviewRequest));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should delete review successfully")
    void deleteReview_Success() {
        // Given
        when(userSecurity.getCurrentUserId()).thenReturn(1L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        reviewService.deleteReview(1L);

        // Then
        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent review")
    void deleteReview_ReviewNotFound() {
        // Given
        when(userSecurity.getCurrentUserId()).thenReturn(1L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.deleteReview(1L));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should get reviews by room with pagination")
    void getReviewsByRoom_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);

        when(reviewRepository.findByRoomIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(reviewPage);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        // When
        PaginationResponse<ReviewResponse> result = reviewService.getReviewsByRoom(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get reviews by user with pagination")
    void getReviewsByUser_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);

        when(userSecurity.getCurrentUserId()).thenReturn(1L);
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(reviewPage);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        // When
        PaginationResponse<ReviewResponse> result = reviewService.getReviewsByUser(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get room review summary successfully")
    void getRoomReviewSummary_Success() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reviewRepository.findAverageRatingByRoomId(1L)).thenReturn(Optional.of(4.5));
        when(reviewRepository.countByRoomId(1L)).thenReturn(10L);

        List<Object[]> ratingDistribution = List.of(
                new Object[]{(byte) 5, 5L},
                new Object[]{(byte) 4, 3L},
                new Object[]{(byte) 3, 2L}
        );
        when(reviewRepository.findRatingDistributionByRoomId(1L))
                .thenReturn(ratingDistribution);

        // When
        ReviewSummaryResponse result = reviewService.getRoomReviewSummary(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRoomId());
        assertEquals("101", result.getRoomNumber());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getTotalReviews());
        assertEquals(5L, result.getFiveStars());
        assertEquals(3L, result.getFourStars());
        assertEquals(2L, result.getThreeStars());
        assertEquals(0L, result.getTwoStars());
        assertEquals(0L, result.getOneStar());
    }

    @Test
    @DisplayName("Should throw exception when getting summary for non-existent room")
    void getRoomReviewSummary_RoomNotFound() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.getRoomReviewSummary(1L));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should get room review summary with default values when no reviews exist")
    void getRoomReviewSummary_NoReviews() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reviewRepository.findAverageRatingByRoomId(1L)).thenReturn(Optional.empty());
        when(reviewRepository.countByRoomId(1L)).thenReturn(0L);
        when(reviewRepository.findRatingDistributionByRoomId(1L)).thenReturn(List.of());

        // When
        ReviewSummaryResponse result = reviewService.getRoomReviewSummary(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRoomId());
        assertEquals(0.0, result.getAverageRating());
        assertEquals(0L, result.getTotalReviews());
        assertEquals(0L, result.getFiveStars());
        assertEquals(0L, result.getFourStars());
        assertEquals(0L, result.getThreeStars());
        assertEquals(0L, result.getTwoStars());
        assertEquals(0L, result.getOneStar());
    }

    @Test
    @DisplayName("Should get single review successfully")
    void getReview_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.getReview(1L);

        // Then
        assertNotNull(result);
        assertEquals(reviewResponse.getId(), result.getId());
        assertEquals(reviewResponse.getRating(), result.getRating());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent review")
    void getReview_ReviewNotFound() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.getReview(1L));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should get all reviews with pagination")
    void getAllReviews_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);

        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        // When
        PaginationResponse<ReviewResponse> result = reviewService.getAllReviews(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertEquals(10, result.getPageSize());
    }

    @Test
    @DisplayName("Should delete review by admin successfully")
    void deleteReviewByAdmin_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        reviewService.deleteReviewByAdmin(1L);

        // Then
        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("Should throw exception when admin deletes non-existent review")
    void deleteReviewByAdmin_ReviewNotFound() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reviewService.deleteReviewByAdmin(1L));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle empty review list for room")
    void getReviewsByRoom_EmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(reviewRepository.findByRoomIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(emptyPage);

        // When
        PaginationResponse<ReviewResponse> result = reviewService.getReviewsByRoom(1L, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Should handle empty review list for user")
    void getReviewsByUser_EmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userSecurity.getCurrentUserId()).thenReturn(1L);
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(emptyPage);

        // When
        PaginationResponse<ReviewResponse> result = reviewService.getReviewsByUser(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Should round average rating correctly")
    void getRoomReviewSummary_RoundingTest() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reviewRepository.findAverageRatingByRoomId(1L)).thenReturn(Optional.of(4.567));
        when(reviewRepository.countByRoomId(1L)).thenReturn(3L);
        when(reviewRepository.findRatingDistributionByRoomId(1L)).thenReturn(List.of());

        // When
        ReviewSummaryResponse result = reviewService.getRoomReviewSummary(1L);

        // Then
        assertEquals(4.6, result.getAverageRating()); // Rounded to 1 decimal place
    }
}
