package com.example.cafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoMoConfig {
    
    // ✅ Thông tin test từ MoMo Sandbox
    @Value("${momo.partner-code:MOMOBKUN20180529}")
    private String partnerCode;
    
    @Value("${momo.access-key:klm05TvNBzhg7h7j}")
    private String accessKey;
    
    @Value("${momo.secret-key:at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa}")
    private String secretKey;
    
    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;
    
    @Value("${momo.return-url:http://localhost:3000/payment/momo/return}")
    private String returnUrl;
    
    @Value("${momo.notify-url:http://localhost:8080/api/payment/momo/notify}")
    private String notifyUrl;
    
    @Value("${momo.request-type:captureWallet}")
    private String requestType;
    
    // Getters
    public String getPartnerCode() {
        return partnerCode;
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public String getNotifyUrl() {
        return notifyUrl;
    }
    
    public String getRequestType() {
        return requestType;
    }
}