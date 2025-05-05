package project.hotel_booking_system.dto.request.room_request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.enums.RoomType;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomCreationRequest {

    String roomNumber;
    RoomType roomType;
    BigDecimal price;
    RoomStatus roomStatus;
    String description;

}
