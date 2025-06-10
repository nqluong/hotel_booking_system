package project.hotel_booking_system.dto.request.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request to create or update a review")
public class ReviewRequest {
    @Schema(description = "ID of the user who made the review", example = "1", required = true)
    @NotNull(message = "User ID is required")
    Long userId;

    @Schema(description = "ID of the room being reviewed", example = "1", required = true)
    @NotNull(message = "Room ID is required")
    Long roomId;

    @Schema(description = "Rating from 1 to 5 stars", example = "5", required = true, minimum = "1", maximum = "5")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    Byte rating;

    @Schema(description = "Optional comment about the room", example = "Great room with excellent service!", maxLength = 1000)
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    String comment;
}
