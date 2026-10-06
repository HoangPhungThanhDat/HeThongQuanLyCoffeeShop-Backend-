package com.example.cafe.services;

import com.example.cafe.config.MoMoConfig;
import com.example.cafe.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService {
    
    private final MoMoConfig momoConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Tạo URL thanh toán MoMo
     */
    public PaymentApiResponse createPaymentUrl(String orderId, Long amount, String orderInfo) {
        try {
            log.info("🔵 Đang tạo MoMo payment URL cho Order: {}, Amount: {}", orderId, amount);
            
            // 1. Tạo requestId unique
            String requestId = UUID.randomUUID().toString();
            
            // 2. Tạo chữ ký HMAC SHA256
            String signature = generateSignature(requestId, orderId, amount, orderInfo); // ✅ Đã thêm orderInfo
            
            // 3. Build request
            MoMoPaymentRequest request = MoMoPaymentRequest.builder()
                    .partnerCode(momoConfig.getPartnerCode())
                    .accessKey(momoConfig.getAccessKey())
                    .requestId(requestId)
                    .amount(amount)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(momoConfig.getReturnUrl())
                    .ipnUrl(momoConfig.getNotifyUrl())
                    .extraData("")
                    .requestType(momoConfig.getRequestType())
                    .signature(signature)
                    .lang("vi")
                    .build();
            
            log.info("📤 Gửi request đến MoMo: {}", objectMapper.writeValueAsString(request));
            
            // 4. Gọi MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<MoMoPaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<MoMoPaymentResponse> response = restTemplate.exchange(
                    momoConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    MoMoPaymentResponse.class
            );
            
            MoMoPaymentResponse momoResponse = response.getBody();
            
            if (momoResponse != null && momoResponse.isSuccess()) {
                log.info("✅ MoMo response thành công: {}", objectMapper.writeValueAsString(momoResponse));
                
                return PaymentApiResponse.builder()
                        .success(true)
                        .message("Tạo thanh toán thành công")
                        .paymentUrl(momoResponse.getPayUrl())
                        .qrCodeUrl(momoResponse.getQrCodeUrl())
                        .data(momoResponse)
                        .build();
            } else {
                log.error("❌ MoMo response thất bại: {}", momoResponse);
                
                return PaymentApiResponse.builder()
                        .success(false)
                        .message(momoResponse != null ? momoResponse.getMessage() : "Lỗi không xác định")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("❌ Exception khi tạo MoMo payment: ", e);
            
            return PaymentApiResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Tạo chữ ký HMAC SHA256
     */
    private String generateSignature(String requestId, String orderId, Long amount, String orderInfo) throws Exception {
        // ✅ Thêm tham số orderInfo vào hàm
        
        // ✅ Format ĐÚNG theo docs MoMo (sắp xếp alphabetically)
        String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" +  // ✅ Để trống (empty string)
                "&ipnUrl=" + momoConfig.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +  // ✅ Dùng orderInfo từ tham số, không hardcode
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + momoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + momoConfig.getRequestType();
        
        log.debug("🔐 Raw signature: {}", rawSignature);
        
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                momoConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        hmacSha256.init(secretKeySpec);
        
        byte[] hash = hmacSha256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder signature = new StringBuilder();
        for (byte b : hash) {
            signature.append(String.format("%02x", b));
        }
        
        String finalSignature = signature.toString();
        log.debug("🔐 Generated signature: {}", finalSignature);
        
        return finalSignature;
    }
    
    /**
     * Verify signature từ MoMo IPN
     */
    public boolean verifySignature(MoMoIPNRequest ipnRequest) {
        try {
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + ipnRequest.getAmount() +
                    "&extraData=" + ipnRequest.getExtraData() +
                    "&message=" + ipnRequest.getMessage() +
                    "&orderId=" + ipnRequest.getOrderId() +
                    "&orderInfo=" + ipnRequest.getOrderInfo() +
                    "&orderType=" + ipnRequest.getOrderType() +
                    "&partnerCode=" + ipnRequest.getPartnerCode() +
                    "&payType=" + ipnRequest.getPayType() +
                    "&requestId=" + ipnRequest.getRequestId() +
                    "&responseTime=" + ipnRequest.getResponseTime() +
                    "&resultCode=" + ipnRequest.getResultCode() +
                    "&transId=" + ipnRequest.getTransId();
            
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    momoConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSha256.init(secretKeySpec);
            
            byte[] hash = hmacSha256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder signature = new StringBuilder();
            for (byte b : hash) {
                signature.append(String.format("%02x", b));
            }
            
            String calculatedSignature = signature.toString();
            boolean isValid = calculatedSignature.equals(ipnRequest.getSignature());
            
            log.info("🔐 Verify signature: Expected={}, Received={}, Valid={}", 
                    calculatedSignature, ipnRequest.getSignature(), isValid);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("❌ Error verifying signature: ", e);
            return false;
        }
    }
}