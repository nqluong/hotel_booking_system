package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundResponseDTO {
    Long id;
    Long paymentId;
    Long bookingId;
    BigDecimal refundAmount;
    RefundStatus status;
    String refundReason;
    String vnpayRefundId;
    LocalDateTime createdAt;
    LocalDateTime processedAt;
}
