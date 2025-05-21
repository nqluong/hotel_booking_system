package project.hotel_booking_system.dto.request.payment_request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequestDTO {
     Long bookingId;
     PaymentMethod paymentMethod;
     boolean isAdvancePayment;
} 