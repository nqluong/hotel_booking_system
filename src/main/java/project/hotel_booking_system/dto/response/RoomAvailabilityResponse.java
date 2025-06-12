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
public class RoomAvailabilityResponse {
    Long roomId;
    String roomNumber;
    List<LocalDate> availableDates;
    List<LocalDate> bookedDates;
    List<LocalDate> blockedDates;
}
