package project.hotel_booking_system.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.RefundEligibilityResponse;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.service.payment.RefundService;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RefundController {

    @Autowired
    RefundService refundService;

    @PostMapping("/process-booking-refund/{bookingId}")
    public ApiResponseDTO<RefundResponseDTO> processBookingRefund(
            @PathVariable Long bookingId) {


        RefundResponseDTO refundResponse = refundService.processBookingRefund(bookingId);

        return ApiResponseDTO.<RefundResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message(refundResponse != null ? "Refund processed successfully" : "No refund applicable")
                .result(refundResponse)
                .build();
    }

    /**
     * Check refund eligibility for a booking
     */
    @GetMapping("/check-eligibility/{bookingId}")
    public ApiResponseDTO<RefundEligibilityResponse> checkRefundEligibility(
            @PathVariable Long bookingId) {


        RefundEligibilityResponse eligibility = refundService.checkRefundEligibility(bookingId);

        return ApiResponseDTO.<RefundEligibilityResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Refund eligibility checked successfully")
                .result(eligibility)
                .build();
    }

    /**
     * Get refund status by booking ID
     */
    @GetMapping("/status/{bookingId}")
    public ApiResponseDTO<RefundResponseDTO> getRefundStatus(
            @PathVariable Long bookingId) {

        RefundResponseDTO refundStatus = refundService.getRefundByBookingId(bookingId);

        return ApiResponseDTO.<RefundResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Refund status retrieved successfully")
                .result(refundStatus)
                .build();
    }
}
