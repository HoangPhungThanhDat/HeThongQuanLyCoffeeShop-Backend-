// package com.example.cafe.controllers;

// import com.example.cafe.entity.Order;
// import com.example.cafe.security.services.OrderService;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/orders")
// public class OrderController {
//     private final OrderService service;

//     public OrderController(OrderService service) {
//         this.service = service;
//     }

//     // Lấy tất cả đơn hàng
//     @GetMapping
//     public ResponseEntity<List<Order>> getAll() {
//         return ResponseEntity.ok(service.findAll());
//     }

//     // Lấy 1 đơn hàng theo id
//     @GetMapping("/{id}")
//     public ResponseEntity<Order> getOne(@PathVariable Long id) {
//         return service.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // Tạo mới đơn hàng
//     @PostMapping
//     public ResponseEntity<Order> create(@RequestBody Order o) {
//         return ResponseEntity.ok(service.save(o));
//     }

//     // ✅ Cập nhật đơn hàng (đã fix lỗi N/A)
//     @PutMapping("/{id}")
//     public ResponseEntity<Order> update(@PathVariable Long id, @RequestBody Order o) {
//         service.update(id, o);

//         // 👉 Sau khi cập nhật xong, lấy lại bản ghi từ DB có đầy đủ các quan hệ
//         return service.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // Xóa đơn hàng
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(@PathVariable Long id) {
//         service.delete(id);
//         return ResponseEntity.noContent().build();
//     }
// }














































package com.example.cafe.controllers;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.entity.Order;
import com.example.cafe.security.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Cho phép CORS từ frontend
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // ==========================================
    // CÁC API GỐC (GIỮ NGUYÊN)
    // ==========================================
    
    /**
     * Lấy tất cả đơn hàng
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Lấy 1 đơn hàng theo id
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo mới đơn hàng
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order o) {
        return ResponseEntity.ok(service.save(o));
    }

    /**
     * Cập nhật đơn hàng (đã fix lỗi N/A)
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Long id, @RequestBody Order o) {
        service.update(id, o);

        // 👉 Sau khi cập nhật xong, lấy lại bản ghi từ DB có đầy đủ các quan hệ
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa đơn hàng
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ✅ API MỚI: THÊM MÓN VÀO ĐƠN HÀNG
    // ==========================================
    
    /**
     * Thêm món vào đơn hàng đã tồn tại
     * POST /api/orders/{orderId}/add-items
     * 
     * Request body example:
     * [
     *   {
     *     "productId": 5,
     *     "quantity": 2
     *   },
     *   {
     *     "productId": 8,
     *     "quantity": 1
     *   }
     * ]
     * 
     * Response example:
     * {
     *   "success": true,
     *   "message": "Thêm món thành công!",
     *   "orderId": 1,
     *   "totalAmount": 250000,
     *   "status": "PENDING",
     *   "totalItems": 5,
     *   "order": { ... }
     * }
     */
    @PostMapping("/{orderId}/add-items")
    public ResponseEntity<?> addItemsToOrder(
            @PathVariable Long orderId,
            @RequestBody List<OrderItemDTO> items
    ) {
        System.out.println("\n📡 ================================================");
        System.out.println("📡 API: POST /api/orders/" + orderId + "/add-items");
        System.out.println("📡 Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("📡 ================================================");
        System.out.println("📦 Số lượng món nhận được: " + (items != null ? items.size() : 0));
        
        // Log chi tiết từng món
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                OrderItemDTO item = items.get(i);
                System.out.println("   " + (i+1) + ". Product ID: " + item.getProductId() + 
                                 " | Quantity: " + item.getQuantity() +
                                 (item.getPrice() != null ? " | Price: " + item.getPrice() : ""));
            }
        }
        
        try {
            // ===== VALIDATION =====
            
            // Kiểm tra orderId
            if (orderId == null || orderId <= 0) {
                System.err.println("❌ Order ID không hợp lệ: " + orderId);
                return createErrorResponse(
                    "Order ID không hợp lệ!", 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Kiểm tra danh sách món
            if (items == null || items.isEmpty()) {
                System.err.println("❌ Danh sách món rỗng!");
                return createErrorResponse(
                    "Danh sách món không được rỗng!", 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Kiểm tra từng món
            for (int i = 0; i < items.size(); i++) {
                OrderItemDTO item = items.get(i);
                if (item.getProductId() == null) {
                    System.err.println("❌ Món " + (i+1) + " thiếu Product ID");
                    return createErrorResponse(
                        "Món thứ " + (i+1) + " thiếu Product ID!", 
                        HttpStatus.BAD_REQUEST
                    );
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    System.err.println("❌ Món " + (i+1) + " có số lượng không hợp lệ");
                    return createErrorResponse(
                        "Món thứ " + (i+1) + " có số lượng không hợp lệ!", 
                        HttpStatus.BAD_REQUEST
                    );
                }
            }
            
            System.out.println("✓ Validation passed");
            
            // ===== GỌI SERVICE XỬ LÝ =====
            System.out.println("\n🔄 Đang gọi service.addItemsToOrder()...");
            Order updatedOrder = service.addItemsToOrder(orderId, items);
            System.out.println("✓ Service xử lý thành công");
            
            // ===== TẠO RESPONSE =====
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm món thành công!");
            response.put("orderId", updatedOrder.getId());
            response.put("totalAmount", updatedOrder.getTotalAmount());
            response.put("status", updatedOrder.getStatus());
            
            // Đếm tổng số món trong đơn
            if (updatedOrder.getOrderItems() != null) {
                response.put("totalItems", updatedOrder.getOrderItems().size());
                System.out.println("📊 Tổng số món trong đơn: " + updatedOrder.getOrderItems().size());
            }
            
            // Thêm thông tin đầy đủ của order
            response.put("order", updatedOrder);
            
            // ===== LOG KẾT QUẢ =====
            System.out.println("\n✅ THÀNH CÔNG!");
            System.out.println("   - Order ID: " + updatedOrder.getId());
            System.out.println("   - Status: " + updatedOrder.getStatus());
            System.out.println("   - New Total: " + updatedOrder.getTotalAmount() + "đ");
            if (updatedOrder.getTable() != null) {
                System.out.println("   - Table: #" + updatedOrder.getTable().getNumber());
            }
            System.out.println("================================================\n");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // Lỗi logic từ service (ví dụ: đơn đã thanh toán, không tìm thấy sản phẩm...)
            System.err.println("\n❌ RUNTIME EXCEPTION:");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            
            // In stack trace để debug
            e.printStackTrace();
            
            System.out.println("================================================\n");
            
            return createErrorResponse(
                e.getMessage(), 
                HttpStatus.BAD_REQUEST
            );
            
        } catch (Exception e) {
            // Lỗi không mong đợi
            System.err.println("\n❌ UNEXPECTED EXCEPTION:");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            
            e.printStackTrace();
            
            System.out.println("================================================\n");
            
            return createErrorResponse(
                "Lỗi server không xác định: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // ==========================================
    // HELPER METHOD
    // ==========================================
    
    /**
     * Tạo response lỗi chuẩn
     */
    private ResponseEntity<?> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        error.put("status", status.value());
        
        return ResponseEntity
                .status(status)
                .body(error);
    }
}