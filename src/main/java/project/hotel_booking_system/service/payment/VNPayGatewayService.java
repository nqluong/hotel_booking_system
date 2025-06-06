package project.hotel_booking_system.service.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayGatewayService {
    String generatePaymentUrl(Long paymentId, BigDecimal amount, String clientIp);

    Map<String, String> parseCallback(String vnPayResponse);

    Long extractPaymentId(String txnRef);

    void removeTransactionMapping(String txnRef);
}
