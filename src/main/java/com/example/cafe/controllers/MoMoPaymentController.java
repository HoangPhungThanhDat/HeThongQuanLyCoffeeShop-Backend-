package com.example.cafe.controllers;

import com.example.cafe.dto.MoMoPaymentRequest;
import com.example.cafe.services.MoMoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/momo")
@CrossOrigin(origins = "*")
public class MoMoPaymentController {

    @Autowired
    private MoMoService moMoService;

    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestBody MoMoPaymentRequest request) {
        
        try {
            System.out.println("\n💳 ==========================================");
            System.out.println("💳 NHẬN REQUEST TẠO THANH TOÁN MOMO");
            System.out.println("💳 ==========================================");
            System.out.println("   - Order ID: " + request.getOrderId());
            System.out.println("   - Amount: " + request.getAmount());
            System.out.println("   - Order Info: " + request.getOrderInfo());
            System.out.println("==========================================\n");

            Map<String, Object> result = moMoService.createPayment(
                request.getOrderId(),
                request.getAmount(),
                request.getOrderInfo()
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Controller Error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi server: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> handleNotify(
            @RequestBody Map<String, Object> notification) {
        
        try {
            System.out.println("\n🔔 ==========================================");
            System.out.println("🔔 NHẬN CALLBACK TỪ MOMO (IPN)");
            System.out.println("🔔 ==========================================");
            System.out.println(new com.google.gson.Gson().toJson(notification));
            System.out.println("==========================================\n");

            // Verify signature
            String signature = (String) notification.get("signature");
            boolean isValid = moMoService.verifySignature(notification, signature);

            if (!isValid) {
                System.err.println("❌ Signature không hợp lệ!");
                return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid signature"));
            }

            // Xử lý cập nhật order status ở đây...
            Integer resultCode = (Integer) notification.get("resultCode");
            String orderId = (String) notification.get("orderId");

            if (resultCode == 0) {
                System.out.println("✅ Thanh toán thành công cho Order: " + orderId);
                // TODO: Update order status to PAID
            } else {
                System.out.println("❌ Thanh toán thất bại cho Order: " + orderId);
                // TODO: Update order status to FAILED
            }

            return ResponseEntity.ok(Map.of("status", "success"));

        } catch (Exception e) {
            System.err.println("❌ Notify Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("status", "error"));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleReturn(@RequestParam Map<String, String> params) {
        System.out.println("\n🔙 ==========================================");
        System.out.println("🔙 USER QUAY LẠI TỪ MOMO");
        System.out.println("🔙 ==========================================");
        System.out.println(new com.google.gson.Gson().toJson(params));
        System.out.println("==========================================\n");

        // Frontend sẽ handle redirect này
        return ResponseEntity.ok("Redirecting...");
    }
}