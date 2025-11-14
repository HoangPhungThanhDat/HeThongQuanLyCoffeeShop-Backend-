package com.example.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về cho Frontend khi tạo thanh toán
 * Đây là response wrapper chung cho tất cả payment APIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApiResponse {
    private boolean success;        // true/false
    private String message;         // Thông báo
    private String paymentUrl;      // URL để redirect user
    private String qrCodeUrl;       // URL QR Code (optional)
    private Object data;            // Dữ liệu bổ sung (optional)
}