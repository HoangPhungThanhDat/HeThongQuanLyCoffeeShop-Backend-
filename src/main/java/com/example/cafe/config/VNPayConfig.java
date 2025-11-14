package com.example.cafe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    // Thông tin từ VNPay Sandbox
    public static final String vnp_TmnCode = "U6YINI4C";
    public static final String vnp_HashSecret = "2JIXD81VG4GAPXXSTMX6R9DJXOEFFUX0";
    public static final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // URL callback (Frontend sẽ nhận kết quả)
    public static final String vnp_ReturnUrl = "http://localhost:3000/payment-result";
    
    // Mã ngôn ngữ
    public static final String vnp_Locale = "vn";
    
    // Phiên bản API
    public static final String vnp_Version = "2.1.0";
    
    // Loại thanh toán
    public static final String vnp_Command = "pay";
    
    // Loại tiền tệ
    public static final String vnp_CurrCode = "VND";
}