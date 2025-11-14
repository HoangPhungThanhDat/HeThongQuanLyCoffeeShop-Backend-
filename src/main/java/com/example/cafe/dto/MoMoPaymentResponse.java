package com.example.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response từ MoMo API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoMoPaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    
    /**
     * Helper method để kiểm tra thanh toán thành công
     * @return true nếu resultCode = 0 (thành công)
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
}