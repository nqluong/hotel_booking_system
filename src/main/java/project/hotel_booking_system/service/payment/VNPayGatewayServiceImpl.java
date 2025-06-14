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
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.dto.response.VNPayRefundResponse;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.RefundStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.RefundMapper;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.model.Refund;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.repository.RefundRepository;

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
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayGatewayServiceImpl implements VNPayGatewayService {

    // Constants
    String DEFAULT_IP = "127.0.0.1";
    String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    String VNPAY_TIMEZONE = "Asia/Ho_Chi_Minh";
    String DATE_FORMAT = "yyyyMMddHHmmss";
    String REFUND_COMMAND = "refund";
    String TRANSACTION_TYPE_REFUND = "03"; //Partial refund
    String SUCCESS_CODE = "00";
    int AMOUNT_MULTIPLIER = 100;

    VnPayConfig vnPayConfig;
    RefundRepository refundRepository;
    PaymentRepository paymentRepository;
    RefundMapper refundMapper;
    RestTemplate restTemplate;

     Map<String, Long> transactionMap = new HashMap<>();

    @Override
    public String generatePaymentUrl(Long paymentId, BigDecimal amount, String clientIp) {
        clientIp = normalizeClientIp(clientIp);

        Payment payment = getPaymentById(paymentId);
        String uniqueTxnRef = generateUniqueTxnRef(paymentId);

        updatePaymentWithTxnRef(payment, uniqueTxnRef);

        Map<String, String> vnpParams = buildPaymentParameters(
                payment, amount, uniqueTxnRef, clientIp
        );

        String paymentUrl = buildPaymentUrl(vnpParams);
        updateTransactionMapping(uniqueTxnRef, paymentId);

        log.info("Generated payment URL for payment ID: {}, txnRef: {}", paymentId, uniqueTxnRef);
        return paymentUrl;
    }

    @Override
    public Map<String, String> parseCallback(String vnPayResponse) {
        Map<String, String> vnpParams = new HashMap<>();

        if (vnPayResponse == null || vnPayResponse.isEmpty()) {
            log.warn("Empty VNPay response received");
            return vnpParams;
        }

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

        validateTxnRef(txnRef);

        if (txnRef.contains("_")) {
            return extractPaymentIdFromCompositeTxnRef(txnRef);
        } else {
            return parsePaymentIdFromString(txnRef);
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
        validateRefundPaymentMethod(refund);

        try {
            updateRefundStatus(refund, RefundStatus.PROCESSING);

            Map<String, String> refundParams = buildRefundParameters(refund);
            VNPayRefundResponse refundResult = callVNPayRefundAPI(refundParams);

            Refund processedRefund = handleRefundResult(refund, refundResult);

            log.info("VNPay refund processed for refund ID: {}, status: {}",
                    refund.getId(), processedRefund.getStatus());

            return refundMapper.toDTO(processedRefund);

        } catch (Exception e) {
            return handleRefundProcessingError(refund, e);
        }
    }

    @Override
    public RefundResponseDTO checkRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));

        return refundMapper.toDTO(refund);
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isEmpty() || LOCALHOST_IPV6.equals(clientIp)) {
            return DEFAULT_IP;
        }
        return clientIp;
    }

    private Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    private String generateUniqueTxnRef(Long paymentId) {
        return paymentId + "_" + System.currentTimeMillis();
    }

    private void updatePaymentWithTxnRef(Payment payment, String txnRef) {
        payment.setVnpTxnRef(txnRef);
        paymentRepository.save(payment);
    }

    private Map<String, String> buildPaymentParameters(Payment payment, BigDecimal amount,
                                                       String txnRef, String clientIp) {
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", formatAmount(amount));
        params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Payment for booking with ID: " + payment.getId());
        params.put("vnp_Locale", vnPayConfig.getLocale());
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_CreateDate", getCurrentTimeVN());
        params.put("vnp_IpAddr", clientIp);

        return params;
    }

    private String formatAmount(BigDecimal amount) {
        return String.valueOf(amount.multiply(BigDecimal.valueOf(AMOUNT_MULTIPLIER))
                .setScale(0, BigDecimal.ROUND_HALF_UP)
                .longValue());
    }

    private String getCurrentTimeVN() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(VNPAY_TIMEZONE));
        return now.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private String buildPaymentUrl(Map<String, String> params) {
        String queryString = buildQueryString(params);
        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryString);

        return vnPayConfig.getPaymentUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    private void validateTxnRef(String txnRef) {
        if (txnRef == null || txnRef.isEmpty()) {
            throw new ResourceNotFoundException("Transaction reference cannot be null or empty");
        }
    }

    private Long extractPaymentIdFromCompositeTxnRef(String txnRef) {
        Long paymentId = getPaymentIdFromTransactionMap(txnRef);
        if (paymentId == null) {
            try {
                paymentId = Long.parseLong(txnRef.split("_")[0]);
                log.warn("Transaction mapping not found, extracted payment ID from txnRef: {}", paymentId);
            } catch (Exception e) {
                log.error("Failed to parse payment ID from transaction reference: {}", txnRef, e);
                throw new ResourceNotFoundException("Invalid transaction reference format: " + txnRef);
            }
        }
        return paymentId;
    }

    private Long parsePaymentIdFromString(String txnRef) {
        try {
            return Long.parseLong(txnRef);
        } catch (NumberFormatException e) {
            log.error("Failed to parse payment ID: {}", txnRef, e);
            throw new ResourceNotFoundException("Invalid payment ID format: " + txnRef);
        }
    }

    private void validateRefundPaymentMethod(Refund refund) {
        if (refund.getPayment().getPaymentMethod() != PaymentMethod.VNPAY) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD_FOR_REFUND);
        }
    }

    private void updateRefundStatus(Refund refund, RefundStatus status) {
        refund.setStatus(status);
        refundRepository.save(refund);
    }

    private Refund handleRefundResult(Refund refund, VNPayRefundResponse refundResult) {
        if (refundResult.isSuccess()) {
            refund.setVnpayRefundId(generateRefundTransactionId());
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());
            log.info("VNPay refund successful for refund ID: {}", refund.getId());
        } else {
            refund.setStatus(RefundStatus.FAILED);
            log.error("VNPay refund failed for refund ID: {}, message: {}",
                    refund.getId(), refundResult.getMessage());
        }

        return refundRepository.save(refund);
    }

    private RefundResponseDTO handleRefundProcessingError(Refund refund, Exception e) {
        log.error("Failed to process VNPay refund for refund ID: {}", refund.getId(), e);
        refund.setStatus(RefundStatus.FAILED);
        Refund failedRefund = refundRepository.save(refund);
        throw new AppException(ErrorCode.REFUND_PROCESSING_FAILED);
    }

    private Map<String, String> buildRefundParameters(Refund refund) {
        Map<String, String> params = new HashMap<>();

        String vnpRequestId = generateRefundRequestId(refund);
        String currentTime = getCurrentTimeVN();

        params.put("vnp_RequestId", vnpRequestId);
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", REFUND_COMMAND);
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_TransactionType", TRANSACTION_TYPE_REFUND);
        params.put("vnp_TxnRef", getOriginalTxnRef(refund));
        params.put("vnp_Amount", formatRefundAmount(refund.getRefundAmount()));
        params.put("vnp_OrderInfo", "Refund for booking ID: " + refund.getBooking().getId());
        params.put("vnp_TransactionNo", refund.getPayment().getTransactionId());
        params.put("vnp_TransactionDate", formatTransactionDate(refund.getPayment().getPaymentDate()));
        params.put("vnp_CreateBy", "User_" + refund.getBooking().getUser().getFullname());
        params.put("vnp_CreateDate", currentTime);
        params.put("vnp_IpAddr", DEFAULT_IP);

        return params;
    }

    private String generateRefundRequestId(Refund refund) {
        return "REF_" + refund.getId() + "_" + System.currentTimeMillis();
    }

    private String formatRefundAmount(BigDecimal refundAmount) {
        return String.valueOf(refundAmount.multiply(BigDecimal.valueOf(AMOUNT_MULTIPLIER)).longValue());
    }

    private String formatTransactionDate(LocalDateTime paymentDate) {
        return paymentDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private VNPayRefundResponse callVNPayRefundAPI(Map<String, String> refundParams) {
        try {
            Map<String, String> requestBody = prepareRefundRequestBody(refundParams);
            HttpEntity<String> request = createRefundHttpRequest(requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(
                    vnPayConfig.getRefundUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            log.info("VNPay refund API response: status={}", response.getStatusCode());
            log.debug("VNPay refund API response body: {}", response.getBody());

            return parseRefundResponse(response.getBody());

        } catch (Exception e) {
            log.error("Error calling VNPay refund API: {}", e.getMessage(), e);
            return VNPayRefundResponse.builder()
                    .success(false)
                    .message("API call exception: " + e.getMessage())
                    .build();
        }
    }

    private Map<String, String> prepareRefundRequestBody(Map<String, String> refundParams) {
        Map<String, String> requestBody = new HashMap<>(refundParams);
        String secureHash = buildRefundSignature(refundParams);
        requestBody.put("vnp_SecureHash", secureHash);
        return requestBody;
    }

    private HttpEntity<String> createRefundHttpRequest(Map<String, String> requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        return new HttpEntity<>(jsonBody, headers);
    }

    private VNPayRefundResponse parseRefundResponse(Map<String, Object> responseBody) {
        try {
            if (responseBody == null) {
                return createFailedRefundResponse("Empty response from VNPay");
            }

            String responseCode = (String) responseBody.get("vnp_ResponseCode");
            String message = (String) responseBody.get("vnp_Message");

            if (SUCCESS_CODE.equals(responseCode)) {
                String transNo = (String) responseBody.get("vnp_TransactionNo");
                return VNPayRefundResponse.builder()
                        .success(true)
                        .transNo(transNo)
                        .message(message)
                        .build();
            } else {
                return createFailedRefundResponse("VNPay error code: " + responseCode + ", Message: " + message);
            }

        } catch (Exception e) {
            log.error("Error parsing VNPay refund response", e);
            return createFailedRefundResponse("Failed to parse response");
        }
    }

    private VNPayRefundResponse createFailedRefundResponse(String message) {
        return VNPayRefundResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private String getOriginalTxnRef(Refund refund) {
        String vnpTxnRef = refund.getPayment().getVnpTxnRef();
        return (vnpTxnRef != null && !vnpTxnRef.isEmpty()) ? vnpTxnRef : refund.getPayment().getId().toString();
    }

    private void updateTransactionMapping(String txnRef, Long paymentId) {
        transactionMap.put(txnRef, paymentId);
        log.info("Added transaction mapping: {} -> {}", txnRef, paymentId);
    }

    private Long getPaymentIdFromTransactionMap(String txnRef) {
        Long paymentId = transactionMap.get(txnRef);
        if (paymentId != null) {
            log.debug("Found payment ID {} for transaction reference {}", paymentId, txnRef);
        }
        return paymentId;
    }

    private String buildRefundSignature(Map<String, String> params) {
        StringBuilder data = new StringBuilder();
        data.append(params.get("vnp_RequestId")).append("|")
                .append(params.get("vnp_Version")).append("|")
                .append(params.get("vnp_Command")).append("|")
                .append(params.get("vnp_TmnCode")).append("|")
                .append(params.get("vnp_TransactionType")).append("|")
                .append(params.get("vnp_TxnRef")).append("|")
                .append(params.get("vnp_Amount")).append("|")
                .append(params.getOrDefault("vnp_TransactionNo", "")).append("|")
                .append(params.get("vnp_TransactionDate")).append("|")
                .append(params.get("vnp_CreateBy")).append("|")
                .append(params.get("vnp_CreateDate")).append("|")
                .append(params.get("vnp_IpAddr")).append("|")
                .append(params.get("vnp_OrderInfo"));

        String dataString = data.toString();
        log.debug("Data for signature: {}", dataString);

        return hmacSHA512(vnPayConfig.getHashSecret(), dataString);
    }

    private String buildQueryString(Map<String, String> params) {
        Map<String, String> filteredParams = new HashMap<>();
        params.entrySet().stream()
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .forEach(entry -> filteredParams.put(entry.getKey(), entry.getValue()));

        List<String> fieldNames = new ArrayList<>(filteredParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
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
                throw new IllegalArgumentException("Key and data cannot be null");
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
            log.error("Error generating HMAC SHA512", ex);
            return "";
        }
    }

    private String generateRefundTransactionId() {
        return "RF_" + System.currentTimeMillis();
    }

}

