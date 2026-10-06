package com.example.cafe.services;

import com.example.cafe.config.VNPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
        try {
            Map<String, String> vnp_Params = new HashMap<>();

            // Thông tin cơ bản
            vnp_Params.put("vnp_Version", vnPayConfig.vnp_Version);
            vnp_Params.put("vnp_Command", vnPayConfig.vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
            vnp_Params.put("vnp_CurrCode", vnPayConfig.vnp_CurrCode);

            // Mã giao dịch (unique)
            String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", vnPayConfig.vnp_OrderType);

            // Ngôn ngữ và URL
            vnp_Params.put("vnp_Locale", vnPayConfig.vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", ipAddress);

            // Thời gian tạo và hết hạn
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Sắp xếp params theo alphabet
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            String paymentUrl = vnPayConfig.vnp_Url + "?" + queryUrl;

            System.out.println("\n🔗 VNPay Payment URL:");
            System.out.println("   TmnCode: " + vnPayConfig.vnp_TmnCode);
            System.out.println("   Amount: " + amount + " VND");
            System.out.println("   TxnRef: " + vnp_TxnRef);
            System.out.println("   URL: " + paymentUrl);

            return paymentUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate callback từ VNPay
     */
    public boolean validateCallback(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();

            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String calculatedHash = hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());

            System.out.println("\n🔐 VNPAY Verify Hash:");
            System.out.println("   From VNPAY: " + vnp_SecureHash);
            System.out.println("   Calculated: " + calculatedHash);
            System.out.println("   Match: " + calculatedHash.equals(vnp_SecureHash));

            return calculatedHash.equals(vnp_SecureHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}