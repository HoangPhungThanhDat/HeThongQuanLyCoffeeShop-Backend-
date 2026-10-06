package com.example.cafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    // ✅ Đọc từ application.properties
    @Value("${vnpay.tmn-code}")
    public String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    public String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    public String vnp_Url;

    @Value("${vnpay.return-url}")
    public String vnp_ReturnUrl;

    @Value("${vnpay.ipn-url}")
    public String vnp_IpnUrl;

    @Value("${vnpay.version}")
    public String vnp_Version;

    @Value("${vnpay.command}")
    public String vnp_Command;

    @Value("${vnpay.order-type}")
    public String vnp_OrderType;

    @Value("${vnpay.locale}")
    public String vnp_Locale;

    @Value("${vnpay.curr-code}")
    public String vnp_CurrCode;
}