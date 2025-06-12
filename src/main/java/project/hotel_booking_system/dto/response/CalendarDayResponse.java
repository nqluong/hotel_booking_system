package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalendarDayResponse {
    LocalDate date;
    List<RoomCalendarInfo> rooms;
}
