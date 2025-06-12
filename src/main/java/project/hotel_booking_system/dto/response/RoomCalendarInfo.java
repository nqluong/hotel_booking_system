package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomCalendarInfo {
    Long roomId;
    String roomNumber;
    String roomType;
    String status;
    String bookingInfo;
}
