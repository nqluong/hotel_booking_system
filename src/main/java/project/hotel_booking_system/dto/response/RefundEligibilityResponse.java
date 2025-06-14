package project.hotel_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundEligibilityResponse {
    boolean eligible;
    String reason;
    BigDecimal refundAmount;
    BigDecimal originalAmount;
    long hoursUntilCheckIn;
    LocalDateTime checkInDate;
    boolean hasVnpayPayment;
}