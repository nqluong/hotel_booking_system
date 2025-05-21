package project.hotel_booking_system.dto.request.booking_request;

import java.util.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreationRequest {
    
    @NotNull(message = "Room ID is required")
    Long roomId;
    
    @NotNull(message = "User ID is required")
    Long userId;
    
    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    Date checkInDate;
    
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    Date checkOutDate;
} 