package project.hotel_booking_system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Review information response")
public class ReviewResponse {
    @Schema(description = "Review ID", example = "1")
    Long id;

    @Schema(description = "User ID who made the review", example = "123")
    Long userId;

    @Schema(description = "Full name of the reviewer", example = "John Doe")
    String userFullname;

    @Schema(description = "Room ID that was reviewed", example = "1")
    Long roomId;

    @Schema(description = "Room number", example = "101")
    String roomNumber;

    @Schema(description = "Rating given (1-5 stars)", example = "5")
    Byte rating;

    @Schema(description = "Review comment", example = "Excellent service and clean room!")
    String comment;

    @Schema(description = "When the review was created", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt;
}
