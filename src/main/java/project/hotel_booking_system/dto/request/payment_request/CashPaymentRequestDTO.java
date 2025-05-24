package project.hotel_booking_system.dto.request.payment_request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CashPaymentRequestDTO {
    
    @NotNull(message = "Booking ID is required")
    Long bookingId;
    
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than 0")
    BigDecimal amount;
    
    @NotNull(message = "Staff confirmation is required")
    Boolean staffConfirmation;
} 