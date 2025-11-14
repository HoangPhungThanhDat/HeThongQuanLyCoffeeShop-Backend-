package com.example.cafe.dto;

public class PaymentRequest {
    private String orderId;
    private long amount; // Số tiền (VND)
    private String orderInfo; // Mô tả đơn hàng
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(String orderId, long amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
