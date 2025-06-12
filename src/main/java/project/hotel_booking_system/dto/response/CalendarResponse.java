package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalendarResponse {
    int year;
    int month;
    List<CalendarDayResponse> days;
}
