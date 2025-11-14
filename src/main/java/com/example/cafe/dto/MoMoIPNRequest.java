package com.example.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho IPN Callback từ MoMo (Server-to-Server)
 * MoMo sẽ gửi request này đến notify-url sau khi user thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoMoIPNRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;           // Transaction ID từ MoMo
    private Integer resultCode;     // 0 = thành công, khác 0 = thất bại
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;       // Chữ ký để verify
}