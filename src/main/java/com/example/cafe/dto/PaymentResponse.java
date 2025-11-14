package com.example.cafe.dto;

public class PaymentResponse {
    private boolean success;
    private String paymentUrl;
    private String message;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(boolean success, String paymentUrl, String message) {
        this.success = success;
        this.paymentUrl = paymentUrl;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}