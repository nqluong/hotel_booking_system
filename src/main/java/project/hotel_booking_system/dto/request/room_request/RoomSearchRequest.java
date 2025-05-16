package project.hotel_booking_system.dto.request.room_request;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RoomType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request model for searching available rooms")
public class RoomSearchRequest {

    @Schema(description = "Check-in date for availability search (Format: yyyy-MM-dd)", example = "2025-06-20")
    Date checkInDate;
    
    @Schema(description = "Check-out date for availability search (Format: yyyy-MM-dd)", example = "2025-06-25")
    Date checkOutDate;

    @Schema(description = "Room type filter (SINGLE, DOUBLE, SUITE)", example = "DOUBLE")
    RoomType roomType;

    @Schema(description = "Minimum price for filtering rooms (in USD)", example = "100.00")
    BigDecimal minPrice;
    
    @Schema(description = "Maximum price for filtering rooms (in USD)", example = "300.00")
    BigDecimal maxPrice;
} 