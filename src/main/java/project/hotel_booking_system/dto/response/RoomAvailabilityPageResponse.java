package project.hotel_booking_system.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomAvailabilityPageResponse {
    Long roomId;
    String roomNumber;
    LocalDate startDate;
    LocalDate endDate;
    int totalAvailableDays;
    int totalBookedDays;
    int totalBlockedDays;
    List<LocalDate> availableDates;
    List<LocalDate> bookedDates;
    List<LocalDate> blockedDates;
}
