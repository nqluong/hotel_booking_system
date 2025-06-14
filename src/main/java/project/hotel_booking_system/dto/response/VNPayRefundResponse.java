package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayRefundResponse {
     boolean success;
     String responseCode;
     String transNo;
     String message;
     String requestId;
}
