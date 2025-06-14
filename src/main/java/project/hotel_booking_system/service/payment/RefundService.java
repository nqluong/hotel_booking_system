package project.hotel_booking_system.service.payment;

import project.hotel_booking_system.dto.response.RefundEligibilityResponse;
import project.hotel_booking_system.dto.response.RefundResponseDTO;

public interface RefundService {
   /**
            * Process refund for booking using only bookingId
     * UserId is extracted from JWT authentication
     */
    RefundResponseDTO processBookingRefund(Long bookingId);

    /**
     * Check refund eligibility for a booking
     */
    RefundEligibilityResponse checkRefundEligibility(Long bookingId);

    /**
     * Get refund information by booking ID
     */
    RefundResponseDTO getRefundByBookingId(Long bookingId);
}
