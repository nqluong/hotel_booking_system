package project.hotel_booking_system.dto.request.payment_request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundRequestDTO {
     Long paymentId;
     Long bookingId;
     BigDecimal refundAmount;
     String refundReason;
     String transactionRef;
}
