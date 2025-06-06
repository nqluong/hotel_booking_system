package project.hotel_booking_system.service.payment;

import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;

public interface CashPaymentService {
    

    //Process a cash payment for a booking
    PaymentResponseDTO processCashPayment(CashPaymentRequestDTO cashPaymentRequestDTO);

    //Get the remaining amount to be paid for a booking
    double getRemainingPaymentAmount(Long bookingId);
} 