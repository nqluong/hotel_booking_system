package project.hotel_booking_system.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.hotel_booking_system.configuration.VnPayConfig;
import project.hotel_booking_system.dto.request.payment_request.RefundRequestDTO;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.dto.response.VNPayRefundResponse;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.RefundStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.RefundMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Refund;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.repository.RefundRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayGatewayServiceImpl implements VNPayGatewayService {

    VnPayConfig vnPayConfig;
    RefundRepository refundRepository;
    PaymentRepository paymentRepository;
    RefundMapper refundMapper;
    RestTemplate restTemplate;

     Map<String, Long> transactionMap = new HashMap<>();

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

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setVnpTxnRef(uniqueTxnRef);
        paymentRepository.save(payment);

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


        String queryString = buildQueryString(vnp_Params);

        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryString);
        String fullUrl = vnPayConfig.getPaymentUrl() + "?" + queryString + "&vnp_SecureHash=" + vnp_SecureHash;

       // String fullUrl = vnPayConfig.getPaymentUrl() + "?" + query;

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

    // Process refund for VNPay payments

    @Override
    public RefundResponseDTO processVNPayRefund(Refund refund) {

        if (refund.getPayment().getPaymentMethod() != PaymentMethod.VNPAY) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD_FOR_REFUND);
        }

        try {
            refund.setStatus(RefundStatus.PROCESSING);
            refundRepository.save(refund);

            // Generate VNPay refund parameters
            Map<String, String> refundParams = buildRefundParameters(refund);

            // Call VNPay refund API
            VNPayRefundResponse refundResult = callVNPayRefundAPI(refundParams);

            if (refundResult.isSuccess()) {
                String vnpayRefundId = generateRefundTransactionId();
                refund.setVnpayRefundId(vnpayRefundId);
                refund.setStatus(RefundStatus.COMPLETED);
                refund.setProcessedAt(LocalDateTime.now());

            } else {
                refund.setStatus(RefundStatus.FAILED);
                log.error("VNPay refund failed for refund ID: {}", refund.getId());
            }

            Refund processedRefund = refundRepository.save(refund);
            return refundMapper.toDTO(processedRefund);

        } catch (Exception e) {
            log.error("Failed to process VNPay refund for refund ID: {}", refund.getId(), e);
            refund.setStatus(RefundStatus.FAILED);
            refundRepository.save(refund);
            throw new AppException(ErrorCode.REFUND_PROCESSING_FAILED);
        }
    }

    @Override
    public RefundResponseDTO checkRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));

        return refundMapper.toDTO(refund);
    }

    private Map<String, String> buildRefundParameters(Refund refund) {
        Map<String, String> params = new HashMap<>();

        // Current time in VN timezone
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Generate unique request ID
        String vnp_RequestId = "REF_" + refund.getId() + "_" + System.currentTimeMillis();

        params.put("vnp_RequestId", vnp_RequestId);
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_TransactionType", "03"); //
        params.put("vnp_TxnRef", getOriginalTxnRef(refund));
        params.put("vnp_Amount", String.valueOf(refund.getRefundAmount()
                .multiply(BigDecimal.valueOf(100)).longValue()));
        params.put("vnp_OrderInfo", "Refund for booking ID: " + refund.getBooking().getId());
        params.put("vnp_TransactionNo", refund.getPayment().getTransactionId());
        params.put("vnp_TransactionDate", refund.getPayment().getPaymentDate().format(formatter));
        params.put("vnp_CreateBy", "User_" + refund.getBooking().getUser().getFullname());
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_IpAddr", "127.0.0.1");

        return params;
    }

    private VNPayRefundResponse callVNPayRefundAPI(Map<String, String> refundParams) {
        try {

            Map<String, String> requestBody = new HashMap<>(refundParams);
            String secureHash = buildRefundSignature(refundParams);
            requestBody.put("vnp_SecureHash", secureHash);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);


            ResponseEntity<Map> response = restTemplate.exchange(
                    vnPayConfig.getRefundUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Modified: Log full response for better debugging
            log.info("VNPay refund API response: status={}, body={}",
                    response.getStatusCode(), response.getBody());
            return parseRefundResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error calling VNPay refund API: {}", e.getMessage(), e);
            return VNPayRefundResponse.builder()
                    .message("API call exception: " + e.getMessage())
                    .build();
        }
    }

    private VNPayRefundResponse parseRefundResponse(Map<String, Object> responseBody) {
        try {

            if (responseBody == null) {
                return VNPayRefundResponse.builder()
                        .success(false)
                        .message("Empty response from VNPay")
                        .build();
            }
            String responseCode = (String) responseBody.get("vnp_ResponseCode");
            String message = (String) responseBody.get("vnp_Message");
            String transNo = (String) responseBody.get("vnp_TransactionNo");

            if ("00".equals(responseCode)) {
                return VNPayRefundResponse.builder()
                        .success(true)
                        .transNo(transNo)
                        .message(message)
                        .build();
            } else {
                return VNPayRefundResponse.builder()
                        .success(false)
                        .message("VNPay error code: " + responseCode + ", Message: " + message)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error parsing VNPay refund response", e);
            return VNPayRefundResponse.builder()
                    .success(false)
                    .message("Failed to parse response")
                    .build();
        }
    }

    private String getOriginalTxnRef(Refund refund) {
        String vnpTxnRef = refund.getPayment().getVnpTxnRef();

        if (vnpTxnRef != null && !vnpTxnRef.isEmpty()) {
            return vnpTxnRef;
        }

        return refund.getPayment().getId().toString();
    }

    private void updateTransactionMapping(String txnRef, Long paymentId) {
        transactionMap.put(txnRef, paymentId);
        log.info("Added transaction mapping: {} -> {}", txnRef, paymentId);
    }

    private Long getPaymentIdFromTransactionMap(String txnRef) {
        Long paymentId = transactionMap.get(txnRef);
        if (paymentId != null) {
            log.info("Found payment ID {} for transaction reference {}", paymentId, txnRef);
        }
        return paymentId;
    }

    private String buildRefundSignature(Map<String, String> params) {
        StringBuilder data = new StringBuilder();
        data.append(params.get("vnp_RequestId")).append("|");
        data.append(params.get("vnp_Version")).append("|");
        data.append(params.get("vnp_Command")).append("|");
        data.append(params.get("vnp_TmnCode")).append("|");
        data.append(params.get("vnp_TransactionType")).append("|");
        data.append(params.get("vnp_TxnRef")).append("|");
        data.append(params.get("vnp_Amount")).append("|");
        data.append(params.getOrDefault("vnp_TransactionNo", "")).append("|"); // Có thể null cho refund
        data.append(params.get("vnp_TransactionDate")).append("|");
        data.append(params.get("vnp_CreateBy")).append("|");
        data.append(params.get("vnp_CreateDate")).append("|");
        data.append(params.get("vnp_IpAddr")).append("|");
        data.append(params.get("vnp_OrderInfo"));

        String dataString = data.toString();
        log.info("Data for signature: {}", dataString);

        return hmacSHA512(vnPayConfig.getHashSecret(), dataString);
    }

    private String buildQueryString(Map<String, String> params) {

        Map<String, String> filteredParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // BỎ QUA vnp_SecureHash khi tạo query string
            if (!"vnp_SecureHash".equals(entry.getKey())) {
                filteredParams.put(entry.getKey(), entry.getValue());
            }
        }

        List<String> fieldNames = new ArrayList<>(filteredParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Error encoding VNPay parameters", e);
                }

                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }

        return query.toString();
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
    private String generateRefundTransactionId() {
        return "RF_" + System.currentTimeMillis();
    }

}

