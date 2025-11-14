package com.example.cafe.controllers;

import com.example.cafe.dto.PaymentRequest;
import com.example.cafe.dto.PaymentResponse;
import com.example.cafe.entity.Order;
import com.example.cafe.entity.Bill;
import com.example.cafe.entity.enums.OrderStatus;
import com.example.cafe.entity.enums.PaymentStatus;
import com.example.cafe.entity.enums.PaymentMethod;
import com.example.cafe.services.VNPayService;
import com.example.cafe.security.services.OrderService;
import com.example.cafe.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String SOCKET_SERVER_URL = "http://localhost:3001/payment-success";

    @PostMapping("/create-vnpay-url")
    public ResponseEntity<?> createPaymentUrl(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("\n🔵 ==========================================");
            System.out.println("🔵 TẠO URL THANH TOÁN VNPAY");
            System.out.println("🔵 ==========================================");
            System.out.println("   - Order ID: " + request.getOrderId());
            System.out.println("   - Amount: " + request.getAmount());
            System.out.println("   - Order Info: " + request.getOrderInfo());
            System.out.println("==========================================\n");
            
            String ipAddress = getClientIp(httpRequest);
            String paymentUrl = vnPayService.createPaymentUrl(
                request.getOrderId(),
                request.getAmount(),
                request.getOrderInfo(),
                ipAddress
            );
            
            if (paymentUrl == null) {
                System.err.println("❌ Không thể tạo URL thanh toán");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Không thể tạo URL thanh toán"));
            }
            
            System.out.println("✅ Đã tạo URL thanh toán thành công\n");
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentUrl(paymentUrl);
            response.setMessage("Tạo URL thanh toán thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo URL thanh toán: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> handleVNPayCallback(@RequestParam Map<String, String> params) {
        
        try {
            System.out.println("\n📥 ==========================================");
            System.out.println("📥 VNPAY CALLBACK RECEIVED");
            System.out.println("📥 ==========================================");
            System.out.println("   - Params: " + params);
            System.out.println("==========================================\n");
            
            boolean isValid = vnPayService.validateCallback(params);
            
            if (!isValid) {
                System.err.println("❌ Invalid secure hash!");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid signature"));
            }
            
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_OrderInfo = params.get("vnp_OrderInfo");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");
            
            String orderId = vnp_TxnRef.split("_")[0];
            
            if ("00".equals(vnp_ResponseCode)) {
                System.out.println("✅ Payment success for order: " + orderId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("orderId", orderId);
                result.put("transactionId", vnp_TransactionNo);
                result.put("amount", Long.parseLong(vnp_Amount) / 100);
                
                return ResponseEntity.ok(result);
                
            } else {
                System.err.println("❌ Payment failed for order: " + orderId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Thanh toán thất bại");
                result.put("orderId", orderId);
                result.put("responseCode", vnp_ResponseCode);
                
                return ResponseEntity.ok(result);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi xử lý callback: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi xử lý callback: " + e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT XỬ LÝ THANH TOÁN THÀNH CÔNG (ĐÃ SỬA CHO PHÙ HỢP VỚI ENUM)
     */
    @PostMapping("/notify-success")
    public ResponseEntity<?> notifyPaymentSuccess(@RequestBody Map<String, Object> paymentData) {
        try {
            Long orderId = Long.parseLong(paymentData.get("orderId").toString());
            String paymentMethodStr = paymentData.get("paymentMethod").toString();
            String transactionNo = paymentData.get("transactionNo") != null 
                ? paymentData.get("transactionNo").toString() 
                : null;
            
            System.out.println("\n💳 ==========================================");
            System.out.println("💳 BACKEND: Xử lý thanh toán thành công");
            System.out.println("💳 ==========================================");
            System.out.println("   - Order ID: " + orderId);
            System.out.println("   - Table Number: " + paymentData.get("tableNumber"));
            System.out.println("   - Amount: " + paymentData.get("amount"));
            System.out.println("   - Payment Method: " + paymentMethodStr);
            System.out.println("   - Transaction No: " + transactionNo);
            System.out.println("==========================================\n");

            // 1️⃣ KIỂM TRA ĐƠN HÀNG
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                System.err.println("❌ Không tìm thấy đơn hàng #" + orderId);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Order not found"
                ));
            }

            // 2️⃣ CẬP NHẬT TRẠNG THÁI ORDER → PAID
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);
            System.out.println("✅ Đã cập nhật trạng thái Order #" + orderId + " → PAID");

            // 3️⃣ TÌM VÀ CẬP NHẬT BILL
            Optional<Bill> billOptional = billRepository.findByOrderId(orderId);
            
            if (billOptional.isPresent()) {
                Bill bill = billOptional.get();
                
                System.out.println("\n💰 ==========================================");
                System.out.println("💰 CẬP NHẬT BILL");
                System.out.println("💰 ==========================================");
                System.out.println("   - Bill ID: " + bill.getId());
                System.out.println("   - Trạng thái cũ: " + bill.getPaymentStatus());
                System.out.println("   - Phương thức cũ: " + bill.getPaymentMethod());
                
                // ✅ CẬP NHẬT PHƯƠNG THỨC THANH TOÁN (Dùng enum PaymentMethod)
                if ("VNPay".equalsIgnoreCase(paymentMethodStr)) {
                    bill.setPaymentMethod(PaymentMethod.MOBILE); // VNPay = MOBILE
                } else if ("CASH".equalsIgnoreCase(paymentMethodStr)) {
                    bill.setPaymentMethod(PaymentMethod.CASH);
                } else if ("CARD".equalsIgnoreCase(paymentMethodStr)) {
                    bill.setPaymentMethod(PaymentMethod.CARD);
                } else {
                    bill.setPaymentMethod(PaymentMethod.MOBILE); // Default
                }
                
                // ✅ CẬP NHẬT TRẠNG THÁI THANH TOÁN (Dùng enum PaymentStatus)
                bill.setPaymentStatus(PaymentStatus.COMPLETED);
                
                // ✅ THÊM GHI CHÚ VỀ GIAO DỊCH VNPAY (OPTIONAL)
                if (transactionNo != null) {
                    String existingNotes = bill.getNotes() != null ? bill.getNotes() : "";
                    String newNote = "VNPay Transaction: " + transactionNo + " - " + LocalDateTime.now();
                    bill.setNotes(existingNotes.isEmpty() ? newNote : existingNotes + "\n" + newNote);
                }
                
                // ✅ LƯU BILL (updatedAt sẽ tự động cập nhật nhờ @PreUpdate)
                billRepository.save(bill);
                
                System.out.println("   - Trạng thái mới: " + bill.getPaymentStatus());
                System.out.println("   - Phương thức mới: " + bill.getPaymentMethod());
                System.out.println("✅ Đã cập nhật Bill #" + bill.getId() + " thành công");
                System.out.println("==========================================\n");
                
            } else {
                System.err.println("⚠️ CẢNH BÁO: Không tìm thấy Bill cho Order #" + orderId);
                System.err.println("   → Bill có thể chưa được tạo hoặc đã bị xóa!");
            }

            // 4️⃣ GỬI THÔNG BÁO ĐẾN SOCKET SERVER
            try {
                System.out.println("📡 Đang gửi thông báo đến Socket Server...");
                
                ResponseEntity<Map> socketResponse = restTemplate.postForEntity(
                    SOCKET_SERVER_URL,
                    paymentData,
                    Map.class
                );

                if (socketResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("✅ Đã gửi thông báo đến Socket Server thành công");
                } else {
                    System.err.println("⚠️ Socket Server trả về status: " + socketResponse.getStatusCode());
                }
            } catch (Exception socketError) {
                System.err.println("⚠️ Không thể kết nối Socket Server: " + socketError.getMessage());
            }

            // 5️⃣ TRẢ VỀ KẾT QUẢ
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Payment processed successfully");
            result.put("orderId", orderId);
            result.put("orderStatus", "PAID");
            result.put("billUpdated", billOptional.isPresent());
            
            System.out.println("✅ Hoàn thành xử lý thanh toán thành công\n");
            
            return ResponseEntity.ok(result);

        } catch (NumberFormatException e) {
            System.err.println("❌ Order ID không hợp lệ: " + paymentData.get("orderId"));
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Invalid order ID format"
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }
}