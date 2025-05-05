package project.hotel_booking_system.dto.request.booking_request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.BookingStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingStatusUpdateDTO {
    BookingStatus status;
} 