package project.hotel_booking_system.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.configuration.VnPayConfig;
import project.hotel_booking_system.exception.ResourceNotFoundException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayGatewayServiceImpl implements VNPayGatewayService {

    VnPayConfig vnPayConfig;
    private static final Map<String, Long> transactionMap = new HashMap<>();

    @Override
    public String generatePaymentUrl(Long paymentId, BigDecimal amount, String clientIp) {
        if (clientIp == null || clientIp.isEmpty() || "0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        String vnp_Version = vnPayConfig.getVersion();
        String vnp_Command = vnPayConfig.getCommand();
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_CurrCode = vnPayConfig.getCurrCode();
        String vnp_Locale = vnPayConfig.getLocale();
        String vnp_OrderType = vnPayConfig.getOrderType();

        // Generate unique crypto transaction for each payment
        String uniqueTxnRef = paymentId + "_" + System.currentTimeMillis();

        String vnp_Amount = String.valueOf(amount.multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);

        // Use unique cryto transaction reference
        vnp_Params.put("vnp_TxnRef", uniqueTxnRef);

        vnp_Params.put("vnp_OrderInfo", "Payment for booking with ID: " + paymentId);

        vnp_Params.put("vnp_Locale", vnp_Locale);

        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

        // Current time in VN timezone
        LocalDateTime vnpCreateDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = vnpCreateDate.format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_IpAddr", clientIp);

        // Sort params
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Error encoding VNPay parameters", e);
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String fullUrl = vnPayConfig.getPaymentUrl() + "?" + query;

        // Save the relationship between uniqueTxnRef and payment ID for callback handling
        updateTransactionMapping(uniqueTxnRef, paymentId);

        return fullUrl;
    }

    @Override
    public Map<String, String> parseCallback(String vnPayResponse) {
        Map<String, String> vnpParams = new HashMap<>();

        String[] params = vnPayResponse.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                vnpParams.put(keyValue[0], keyValue[1]);
            }
        }

        return vnpParams;
    }

    @Override
    public Long extractPaymentId(String txnRef) {
        if (txnRef.contains("_")) {
            Long paymentId = getPaymentIdFromTransactionMap(txnRef);
            if (paymentId == null) {
                try {
                    paymentId = Long.parseLong(txnRef.split("_")[0]);
                    log.warn("Transaction mapping not found, extracted payment ID from transaction reference: {}", paymentId);
                    return paymentId;
                } catch (Exception e) {
                    log.error("Failed to parse payment ID from transaction reference: {}", txnRef, e);
                    throw new ResourceNotFoundException("Invalid transaction reference: " + txnRef);
                }
            }
            return paymentId;
        } else {
            try {
                return Long.parseLong(txnRef);
            } catch (NumberFormatException e) {
                log.error("Failed to parse payment ID: {}", txnRef, e);
                throw new ResourceNotFoundException("Invalid payment ID: " + txnRef);
            }
        }
    }

    @Override
    public void removeTransactionMapping(String txnRef) {
        if (transactionMap.containsKey(txnRef)) {
            transactionMap.remove(txnRef);
            log.info("Removed transaction mapping for: {}", txnRef);
        }
    }

    private void updateTransactionMapping(String txnRef, Long paymentId) {
        transactionMap.put(txnRef, paymentId);
        log.info("Added transaction mapping: {} -> {}", txnRef, paymentId);
    }

    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {

            return "";
        }
    }

    private Long getPaymentIdFromTransactionMap(String txnRef) {
        Long paymentId = transactionMap.get(txnRef);
        if (paymentId != null) {
            log.info("Found payment ID {} for transaction reference {}", paymentId, txnRef);
        }
        return paymentId;
    }

}

