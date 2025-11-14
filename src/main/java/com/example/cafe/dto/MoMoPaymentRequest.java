package com.example.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gửi đến MoMo API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoMoPaymentRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private Long amount;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String extraData;
    private String requestType;
    private String signature;
    private String lang;
}