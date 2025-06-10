package project.hotel_booking_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Summary of reviews for a room")
public class ReviewSummaryResponse {
    @Schema(description = "Room ID", example = "1")
    Long roomId;

    @Schema(description = "Room number", example = "101")
    String roomNumber;

    @Schema(description = "Average rating", example = "4.5")
    Double averageRating;

    @Schema(description = "Total number of reviews", example = "25")
    Long totalReviews;

    @Schema(description = "Number of 5-star reviews", example = "15")
    Long fiveStars;

    @Schema(description = "Number of 4-star reviews", example = "8")
    Long fourStars;

    @Schema(description = "Number of 3-star reviews", example = "2")
    Long threeStars;

    @Schema(description = "Number of 2-star reviews", example = "0")
    Long twoStars;

    @Schema(description = "Number of 1-star reviews", example = "0")
    Long oneStar;
}
