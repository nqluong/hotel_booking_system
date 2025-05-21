package project.hotel_booking_system.dto.request.payment_request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.hotel_booking_system.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long bookingId;
    private PaymentMethod paymentMethod;
    private boolean isAdvancePayment;
} 