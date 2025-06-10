package project.hotel_booking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import project.hotel_booking_system.config.TestSecurityConfig;
import project.hotel_booking_system.dto.request.review.ReviewRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.ReviewResponse;
import project.hotel_booking_system.dto.response.ReviewSummaryResponse;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.service.review.ReviewService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;
    private ReviewSummaryResponse reviewSummaryResponse;
    private PaginationResponse<ReviewResponse> paginationResponse;

    @BeforeEach
    void setUp() {
        reviewRequest = ReviewRequest.builder()
                .userId(1L)
                .roomId(1L)
                .rating((byte) 5)
                .comment("Excellent service and clean room!")
                .build();

        reviewResponse = ReviewResponse.builder()
                .id(1L)
                .userId(1L)
                .userFullname("John Doe")
                .roomId(1L)
                .roomNumber("101")
                .rating((byte) 5)
                .comment("Excellent service and clean room!")
                .createdAt(LocalDateTime.now())
                .build();

        reviewSummaryResponse = ReviewSummaryResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .averageRating(4.5)
                .totalReviews(25L)
                .fiveStars(15L)
                .fourStars(8L)
                .threeStars(2L)
                .twoStars(0L)
                .oneStar(0L)
                .build();

        List<ReviewResponse> reviews = Arrays.asList(reviewResponse);
        paginationResponse = PaginationResponse.<ReviewResponse>builder()
                .content(reviews)
                .page(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .build();
    }

    @Test
    @DisplayName("Should create review successfully")
    @WithMockUser(roles = "CUSTOMER")
    void createReview_Success() throws Exception {
        // Given
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(reviewResponse);

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Review created successfully"))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.userId").value(1))
                .andExpect(jsonPath("$.result.roomId").value(1))
                .andExpect(jsonPath("$.result.rating").value(5))
                .andExpect(jsonPath("$.result.comment").value("Excellent service and clean room!"));

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when create review with invalid data")
    @WithMockUser(roles = "CUSTOMER")
    void createReview_InvalidData() throws Exception {
        // Given
        ReviewRequest invalidRequest = ReviewRequest.builder()
                .userId(null)
                .roomId(null)
                .rating((byte) 6) // Invalid rating
                .comment("")
                .build();

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should return 403 when user already reviewed")
    @WithMockUser(roles = "CUSTOMER")
    void createReview_AlreadyReviewed() throws Exception {
        // Given
        when(reviewService.createReview(any(ReviewRequest.class)))
                .thenThrow(new AppException(ErrorCode.REVIEW_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isConflict());

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should update review successfully")
    @WithMockUser(roles = "CUSTOMER")
    void updateReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        when(reviewService.updateReview(eq(reviewId), any(ReviewRequest.class)))
                .thenReturn(reviewResponse);

        // When & Then
        mockMvc.perform(put("/reviews/{reviewId}", reviewId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Review updated successfully"))
                .andExpect(jsonPath("$.result.id").value(1));

        verify(reviewService, times(1)).updateReview(eq(reviewId), any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should return 404 when update non-existent review")
    @WithMockUser(roles = "CUSTOMER")
    void updateReview_NotFound() throws Exception {
        // Given
        Long reviewId = 999L;
        when(reviewService.updateReview(eq(reviewId), any(ReviewRequest.class)))
                .thenThrow(new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // When & Then
        mockMvc.perform(put("/reviews/{reviewId}", reviewId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).updateReview(eq(reviewId), any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should delete review successfully")
    @WithMockUser(roles = "CUSTOMER")
    void deleteReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        doNothing().when(reviewService).deleteReview(reviewId);

        // When & Then
        mockMvc.perform(delete("/reviews/update/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));

        verify(reviewService, times(1)).deleteReview(reviewId);
    }

    @Test
    @DisplayName("Should return 404 when delete non-existent review")
    @WithMockUser(roles = "CUSTOMER")
    void deleteReview_NotFound() throws Exception {
        // Given
        Long reviewId = 999L;
        doThrow(new AppException(ErrorCode.REVIEW_NOT_FOUND))
                .when(reviewService).deleteReview(reviewId);

        // When & Then
        mockMvc.perform(delete("/reviews/update/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).deleteReview(reviewId);
    }

    @Test
    @DisplayName("Should get reviews by room successfully")
    @WithMockUser // Thêm authentication mock
    void getReviewsByRoom_Success() throws Exception {
        // Given
        Long roomId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        when(reviewService.getReviewsByRoom(eq(roomId), any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/reviews/room/{roomId}", roomId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reviews retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.content[0].id").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1));

        verify(reviewService, times(1)).getReviewsByRoom(eq(roomId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get my reviews successfully")
    @WithMockUser(roles = "CUSTOMER")
    void getMyReviews_Success() throws Exception {
        // Given
        when(reviewService.getReviewsByUser(any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/reviews/my-reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reviews retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.totalElements").value(1));

        verify(reviewService, times(1)).getReviewsByUser(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get room review summary successfully")
    @WithMockUser // Thêm authentication mock
    void getRoomReviewSummary_Success() throws Exception {
        // Given
        Long roomId = 1L;
        when(reviewService.getRoomReviewSummary(roomId))
                .thenReturn(reviewSummaryResponse);

        // When & Then
        mockMvc.perform(get("/reviews/room/{roomId}/summary", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Review summary retrieved successfully"))
                .andExpect(jsonPath("$.result.roomId").value(1))
                .andExpect(jsonPath("$.result.averageRating").value(4.5))
                .andExpect(jsonPath("$.result.totalReviews").value(25))
                .andExpect(jsonPath("$.result.fiveStars").value(15));

        verify(reviewService, times(1)).getRoomReviewSummary(roomId);
    }

    @Test
    @DisplayName("Should return 404 when get summary for non-existent room")
    @WithMockUser // Thêm authentication mock
    void getRoomReviewSummary_RoomNotFound() throws Exception {
        // Given
        Long roomId = 999L;
        when(reviewService.getRoomReviewSummary(roomId))
                .thenThrow(new AppException(ErrorCode.ROOM_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/reviews/room/{roomId}/summary", roomId))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getRoomReviewSummary(roomId);
    }

    @Test
    @DisplayName("Should get review by ID successfully")
    @WithMockUser // Thêm authentication mock
    void getReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        when(reviewService.getReview(reviewId)).thenReturn(reviewResponse);

        // When & Then
        mockMvc.perform(get("/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Review retrieved successfully"))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.rating").value(5));

        verify(reviewService, times(1)).getReview(reviewId);
    }

    @Test
    @DisplayName("Should return 404 when get non-existent review")
    @WithMockUser // Thêm authentication mock
    void getReview_NotFound() throws Exception {
        // Given
        Long reviewId = 999L;
        when(reviewService.getReview(reviewId))
                .thenThrow(new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/reviews/{reviewId}", reviewId))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getReview(reviewId);
    }

    @Test
    @DisplayName("Should get all reviews successfully (Admin)")
    @WithMockUser(roles = "ADMIN")
    void getAllReviews_Success() throws Exception {
        // Given
        when(reviewService.getAllReviews(any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/reviews/admin/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("All reviews retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.totalElements").value(1));

        verify(reviewService, times(1)).getAllReviews(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to get all reviews")
    @WithMockUser(roles = "CUSTOMER")
    void getAllReviews_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/reviews/admin/all"))
                .andExpect(status().isForbidden());

        verify(reviewService, never()).getAllReviews(any(Pageable.class));
    }

    @Test
    @DisplayName("Should delete review by admin successfully")
    @WithMockUser(roles = "ADMIN")
    void deleteReviewByAdmin_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        doNothing().when(reviewService).deleteReviewByAdmin(reviewId);

        // When & Then
        mockMvc.perform(delete("/reviews/admin/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));

        verify(reviewService, times(1)).deleteReviewByAdmin(reviewId);
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to delete review")
    @WithMockUser(roles = "CUSTOMER")
    void deleteReviewByAdmin_Forbidden() throws Exception {
        // Given
        Long reviewId = 1L;

        // When & Then
        mockMvc.perform(delete("/reviews/admin/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(reviewService, never()).deleteReviewByAdmin(reviewId);
    }

    @Test
    @DisplayName("Should return 404 when admin tries to delete non-existent review")
    @WithMockUser(roles = "ADMIN")
    void deleteReviewByAdmin_NotFound() throws Exception {
        // Given
        Long reviewId = 999L;
        doThrow(new AppException(ErrorCode.REVIEW_NOT_FOUND))
                .when(reviewService).deleteReviewByAdmin(reviewId);

        // When & Then
        mockMvc.perform(delete("/reviews/admin/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).deleteReviewByAdmin(reviewId);
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to create review")
    void createReview_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isUnauthorized());

        verify(reviewService, never()).createReview(any(ReviewRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to get my reviews")
    void getMyReviews_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/reviews/my-reviews"))
                .andExpect(status().isUnauthorized());

        verify(reviewService, never()).getReviewsByUser(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle pagination parameters correctly")
    @WithMockUser // Thêm authentication mock
    void getReviewsByRoom_WithPagination() throws Exception {
        // Given
        Long roomId = 1L;
        int page = 2;
        int size = 5;

        when(reviewService.getReviewsByRoom(eq(roomId), any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/reviews/room/{roomId}", roomId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());

        // Verify that the correct Pageable was passed
        verify(reviewService, times(1)).getReviewsByRoom(eq(roomId), eq(PageRequest.of(page, size)));
    }

    @Test
    @DisplayName("Should use default pagination when no parameters provided")
    @WithMockUser // Thêm authentication mock
    void getReviewsByRoom_DefaultPagination() throws Exception {
        // Given
        Long roomId = 1L;
        when(reviewService.getReviewsByRoom(eq(roomId), any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/reviews/room/{roomId}", roomId))
                .andExpect(status().isOk());

        // Verify default pagination (page=0, size=10)
        verify(reviewService, times(1)).getReviewsByRoom(eq(roomId), eq(PageRequest.of(0, 10)));
    }
}