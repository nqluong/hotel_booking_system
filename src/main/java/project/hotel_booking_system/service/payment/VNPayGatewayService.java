package project.hotel_booking_system.service.payment;

import project.hotel_booking_system.dto.request.payment_request.RefundRequestDTO;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.model.Refund;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayGatewayService {
    //Payment  methods
    String generatePaymentUrl(Long paymentId, BigDecimal amount, String clientIp);

    Map<String, String> parseCallback(String vnPayResponse);

    Long extractPaymentId(String txnRef);

    void removeTransactionMapping(String txnRef);

    // Refund  methods

    RefundResponseDTO processVNPayRefund(Refund refund);

    RefundResponseDTO checkRefundStatus(Long refundId);

}
