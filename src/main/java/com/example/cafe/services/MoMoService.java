package com.example.cafe.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MoMoService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.return-url}")
    private String returnUrl;

    @Value("${momo.notify-url}")
    private String notifyUrl;

    public Map<String, Object> createPayment(String orderId, Long amount, String orderInfo) {
        try {
            // ✅ LOG ĐỂ DEBUG
            System.out.println("\n🔐 ==========================================");
            System.out.println("🔐 MOMO CONFIGURATION CHECK");
            System.out.println("🔐 ==========================================");
            System.out.println("   - Partner Code: " + partnerCode);
            System.out.println("   - Access Key: " + (accessKey != null ? accessKey.substring(0, Math.min(10, accessKey.length())) + "..." : "NULL"));
            System.out.println("   - Secret Key: " + (secretKey != null ? secretKey.substring(0, Math.min(10, secretKey.length())) + "..." : "NULL"));
            System.out.println("   - Endpoint: " + endpoint);
            System.out.println("   - Return URL: " + returnUrl);
            System.out.println("   - Notify URL: " + notifyUrl);
            System.out.println("==========================================\n");

            // ✅ KIỂM TRA NULL
            if (partnerCode == null || accessKey == null || secretKey == null) {
                throw new RuntimeException("❌ MoMo credentials chưa được cấu hình! Kiểm tra Railway Environment Variables.");
            }

            String requestId = UUID.randomUUID().toString();
            String extraData = ""; // ✅ Empty string, NOT null

            // ✅ QUAN TRỌNG: Thứ tự params theo MoMo docs
            String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=captureWallet",
                accessKey,
                amount,
                extraData,
                notifyUrl,
                orderId,
                orderInfo,
                partnerCode,
                returnUrl,
                requestId
            );

            System.out.println("\n📝 Raw Signature String:");
            System.out.println(rawSignature);

            // ✅ TẠO SIGNATURE
            String signature = generateHmacSHA256(rawSignature, secretKey);
            System.out.println("\n✅ Generated Signature: " + signature);

            // ✅ TẠO REQUEST BODY (theo thứ tự alphabet - best practice)
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("accessKey", accessKey);
            requestBody.put("amount", amount);
            requestBody.put("extraData", extraData);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("lang", "vi");
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("partnerName", "Coffee Shop");
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("requestId", requestId);
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            requestBody.put("storeId", "CoffeeShop01");

            System.out.println("\n📦 Request Body:");
            System.out.println(new com.google.gson.Gson().toJson(requestBody));

            // ✅ GỬI REQUEST
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                endpoint,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            System.out.println("\n📥 MoMo Response:");
            System.out.println(new com.google.gson.Gson().toJson(responseBody));

            // ✅ XỬ LÝ RESPONSE
            Integer resultCode = (Integer) responseBody.get("resultCode");

            Map<String, Object> result = new HashMap<>();
            
            if (resultCode != null && resultCode == 0) {
                result.put("success", true);
                result.put("paymentUrl", responseBody.get("payUrl"));
                result.put("orderId", orderId);
                result.put("message", "Tạo thanh toán MoMo thành công");
            } else {
                result.put("success", false);
                result.put("message", responseBody.get("message"));
                result.put("resultCode", resultCode);
                
                System.err.println("❌ MoMo Error Code: " + resultCode);
                System.err.println("❌ MoMo Message: " + responseBody.get("message"));
            }

            return result;

        } catch (Exception e) {
            System.err.println("\n❌ ==========================================");
            System.err.println("❌ MOMO SERVICE ERROR");
            System.err.println("❌ ==========================================");
            e.printStackTrace();
            System.err.println("❌ ==========================================\n");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi hệ thống: " + e.getMessage());
            return errorResponse;
        }
    }

    // ✅ HÀM TẠO HMAC SHA256
    private String generateHmacSHA256(String data, String key) throws Exception {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            hmacSHA256.init(secretKeySpec);
            
            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("❌ Error generating HMAC signature: " + e.getMessage());
            throw e;
        }
    }

    // ✅ VERIFY SIGNATURE TỪ MOMO CALLBACK
    public boolean verifySignature(Map<String, Object> params, String signature) {
        try {
            // Tạo lại raw signature từ params
            String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                params.get("accessKey"),
                params.get("amount"),
                params.get("extraData"),
                params.get("message"),
                params.get("orderId"),
                params.get("orderInfo"),
                params.get("orderType"),
                params.get("partnerCode"),
                params.get("payType"),
                params.get("requestId"),
                params.get("responseTime"),
                params.get("resultCode"),
                params.get("transId")
            );

            String calculatedSignature = generateHmacSHA256(rawSignature, secretKey);
            
            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("❌ Error verifying signature: " + e.getMessage());
            return false;
        }
    }
}