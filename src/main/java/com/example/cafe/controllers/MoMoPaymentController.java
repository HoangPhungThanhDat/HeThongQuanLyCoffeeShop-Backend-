package com.example.cafe.controllers;

import com.example.cafe.dto.*;
import com.example.cafe.services.MoMoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/momo")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MoMoPaymentController {
    
    private final MoMoService momoService;
    
    /**
     * ✅ API 1: Tạo URL thanh toán MoMo
     * Frontend sẽ gọi API này để lấy paymentUrl
     * Endpoint: POST /api/momo/create-payment
     */
    @PostMapping("/create-payment")
    public ResponseEntity<PaymentApiResponse> createMoMoPayment(@RequestBody MoMoPaymentRequestDto request) {
        log.info("📥 Nhận request tạo MoMo payment: {}", request);
        
        try {
            // Validate
            if (request.getOrderId() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body(
                    PaymentApiResponse.builder()
                        .success(false)
                        .message("OrderId và Amount không được để trống")
                        .build()
                );
            }
            
            // Tạo orderInfo
            String orderInfo = request.getOrderInfo() != null 
                ? request.getOrderInfo() 
                : "Thanh toán đơn hàng #" + request.getOrderId();
            
            // Gọi service
            PaymentApiResponse response = momoService.createPaymentUrl(
                request.getOrderId(),
                request.getAmount(),
                orderInfo
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Lỗi tạo MoMo payment: ", e);
            
            return ResponseEntity.internalServerError().body(
                PaymentApiResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * ✅ API 2: IPN Callback từ MoMo (Server-to-Server)
     * MoMo sẽ gọi API này để thông báo kết quả thanh toán
     * Endpoint: POST /api/momo/ipn-callback
     */
    @PostMapping("/ipn-callback")
    public ResponseEntity<Map<String, Object>> handleMoMoIPN(@RequestBody MoMoIPNRequest ipnRequest) {
        log.info("📥 ===== NHẬN IPN TỪ MOMO =====");
        log.info("📦 IPN Data: {}", ipnRequest);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Verify signature
            boolean isValidSignature = momoService.verifySignature(ipnRequest);
            
            if (!isValidSignature) {
                log.error("❌ Signature không hợp lệ!");
                response.put("status", "error");
                response.put("message", "Invalid signature");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2. Kiểm tra kết quả thanh toán
            if (ipnRequest.getResultCode() == 0) {
                log.info("✅ THANH TOÁN THÀNH CÔNG - Order: {}, TransId: {}", 
                        ipnRequest.getOrderId(), ipnRequest.getTransId());
                
                // TODO: Cập nhật trạng thái đơn hàng trong DB
                // orderService.updatePaymentStatus(ipnRequest.getOrderId(), "PAID");
                
                // TODO: Emit socket event để thông báo cho frontend
                // socketService.emit("payment-success", ipnRequest.getOrderId());
                
                response.put("status", "success");
                response.put("message", "Payment processed successfully");
                
            } else {
                log.warn("⚠️ THANH TOÁN THẤT BẠI - Order: {}, ResultCode: {}, Message: {}", 
                        ipnRequest.getOrderId(), ipnRequest.getResultCode(), ipnRequest.getMessage());
                
                // TODO: Cập nhật trạng thái thanh toán thất bại
                
                response.put("status", "failed");
                response.put("message", ipnRequest.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý IPN: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ✅ API 3: Return URL (User redirect về từ MoMo)
     * Đây là endpoint để handle khi user quay lại từ app MoMo
     * Frontend sẽ check query params và hiển thị kết quả
     * Endpoint: GET /api/momo/return
     */
    @GetMapping("/return")
    public String handleMoMoReturn(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer resultCode,
            @RequestParam(required = false) String message) {
        
        log.info("📥 User quay lại từ MoMo - Order: {}, ResultCode: {}", orderId, resultCode);
        
        // ✅ LOCAL TEST: Tự động cập nhật trạng thái vì IPN không hoạt động
        if (resultCode != null && resultCode == 0) {
            log.info("✅ [LOCAL TEST] Thanh toán thành công - Tự động cập nhật Order: {}", orderId);
            // TODO: Uncomment khi có OrderService
            // orderService.updatePaymentStatus(orderId, "PAID");
        }
        
        // Redirect về frontend với query params
        String frontendUrl = "http://localhost:3000/payment/momo/return";
        return "redirect:" + frontendUrl + 
               "?orderId=" + orderId + 
               "&resultCode=" + resultCode + 
               "&message=" + message;
    }
    
    /**
     * ✅ API 4: Kiểm tra trạng thái thanh toán
     * Endpoint: GET /api/momo/check-status/{orderId}
     */
    @GetMapping("/check-status/{orderId}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String orderId) {
        log.info("📥 Kiểm tra trạng thái thanh toán cho Order: {}", orderId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: Implement logic check từ DB
            // PaymentStatus status = paymentService.getPaymentStatus(orderId);
            
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("status", "PENDING"); // TODO: Get from DB
            response.put("message", "Payment status retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Lỗi kiểm tra trạng thái: ", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

// ========== Request DTO ==========
@lombok.Data
class MoMoPaymentRequestDto {
    private String orderId;
    private Long amount;
    private String orderInfo;
}