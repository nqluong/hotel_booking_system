package project.hotel_booking_system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.enums.RoomType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomResponse {

    Long id;
    String roomNumber;
    RoomType roomType;
    BigDecimal price;
    RoomStatus roomStatus;
    String description;
    LocalDateTime createdAt;
    List<RoomImageResponse> images;
}
