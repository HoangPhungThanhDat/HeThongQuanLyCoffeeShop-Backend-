// package com.example.cafe.security.services.impl;

// import com.example.cafe.dto.OrderItemDTO;
// import com.example.cafe.dto.PageResponse;
// import com.example.cafe.entity.Order;
// import com.example.cafe.entity.OrderItem;
// import com.example.cafe.entity.Product;
// import com.example.cafe.entity.enums.OrderStatus;
// import com.example.cafe.entity.Bill;
// import com.example.cafe.repository.OrderRepository;
// import com.example.cafe.repository.ProductRepository;
// import com.example.cafe.repository.TableRepository;
// import com.example.cafe.repository.UserRepository;
// import com.example.cafe.repository.OrderItemRepository;
// import com.example.cafe.repository.BillRepository;
// import com.example.cafe.security.services.OrderService;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// @Service
// public class OrderServiceImpl implements OrderService {

//     private final OrderRepository repo;
//     private final TableRepository tableRepo;
//     private final UserRepository userRepo;
//     private final ProductRepository productRepo;
//     private final OrderItemRepository orderItemRepo;
//     private final BillRepository billRepo;

//     public OrderServiceImpl(
//             OrderRepository repo,
//             TableRepository tableRepo,
//             UserRepository userRepo,
//             ProductRepository productRepo,
//             OrderItemRepository orderItemRepo,
//             BillRepository billRepo) {
//         this.repo = repo;
//         this.tableRepo = tableRepo;
//         this.userRepo = userRepo;
//         this.productRepo = productRepo;
//         this.orderItemRepo = orderItemRepo;
//         this.billRepo = billRepo;
//     }

//     // ==========================================
//     // ✅ CÁC METHOD CŨ (GIỮ NGUYÊN 100%)
//     // ==========================================

//     @Override
//     public Order save(Order o) {
//         if (o.getTable() != null && o.getTable().getId() != null) {
//             tableRepo.findById(o.getTable().getId()).ifPresent(o::setTable);
//         }
//         if (o.getEmployee() != null && o.getEmployee().getId() != null) {
//             userRepo.findById(o.getEmployee().getId()).ifPresent(o::setEmployee);
//         }

//         if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
//             BigDecimal total = BigDecimal.ZERO;
//             for (var item : o.getOrderItems()) {
//                 if (item.getProduct() != null && item.getProduct().getId() != null) {
//                     Product p = productRepo.findById(item.getProduct().getId())
//                             .orElseThrow(() -> new RuntimeException("Product not found"));
//                     item.setProduct(p);
//                     item.setPrice(p.getPrice());
//                     item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
//                 }
//                 total = total.add(item.getSubtotal());
//             }
//             o.setTotalAmount(total);
//         }

//         return repo.save(o);
//     }

//     @Override
//     public Order update(Long id, Order o) {
//         return repo.findById(id).map(existing -> {
//             existing.setStatus(o.getStatus());
//             existing.setNotes(o.getNotes());
//             existing.setPromotion(o.getPromotion());
//             existing.setTotalAmount(o.getTotalAmount());

//             if (o.getTable() != null && o.getTable().getId() != null) {
//                 tableRepo.findById(o.getTable().getId()).ifPresent(existing::setTable);
//             }

//             if (o.getEmployee() != null && o.getEmployee().getId() != null) {
//                 userRepo.findById(o.getEmployee().getId()).ifPresent(existing::setEmployee);
//             }

//             if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
//                 BigDecimal total = BigDecimal.ZERO;
//                 for (var item : o.getOrderItems()) {
//                     if (item.getProduct() != null && item.getProduct().getId() != null) {
//                         Product p = productRepo.findById(item.getProduct().getId())
//                                 .orElseThrow(() -> new RuntimeException("Product not found"));
//                         item.setProduct(p);
//                         item.setPrice(p.getPrice());
//                         item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
//                     }
//                     total = total.add(item.getSubtotal());
//                 }
//                 existing.setOrderItems(o.getOrderItems());
//                 existing.setTotalAmount(total);
//             }

//             return repo.save(existing);
//         }).orElseThrow(() -> new RuntimeException("Order not found"));
//     }

//     @Override
//     public void delete(Long id) {
//         repo.deleteById(id);
//     }

//     @Override
//     public Optional<Order> findById(Long id) {
//         return repo.findById(id);
//     }

//     @Override
//     public List<Order> findAll() {
//         return repo.findAll();
//     }

//     @Override
//     public Order getOrderById(Long id) {
//         return repo.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
//     }

//     @Override
//     @Transactional
//     public Order updateOrderStatus(Long id, OrderStatus status) {
//         Order order = getOrderById(id);
//         order.setStatus(status);
//         Order savedOrder = repo.save(order);

//         System.out.println("💾 Đã lưu trạng thái mới vào database:");
//         System.out.println("   - Order ID: " + id);
//         System.out.println("   - New Status: " + status);

//         return savedOrder;
//     }

//     // ==========================================
//     // ✅ METHOD THÊM MÓN VÀO ĐƠN HÀNG (GIỮ NGUYÊN)
//     // ==========================================
//     @Override
//     @Transactional
//     public Order addItemsToOrder(Long orderId, List<OrderItemDTO> itemDTOs) {
//         System.out.println("\n🔄 ==========================================");
//         System.out.println("🔄 BACKEND: addItemsToOrder");
//         System.out.println("🔄 ==========================================");
//         System.out.println("📦 Order ID: " + orderId);
//         System.out.println("📦 Số món nhận được: " + itemDTOs.size());

//         // ✅ 1. VALIDATE INPUT
//         if (itemDTOs == null || itemDTOs.isEmpty()) {
//             throw new RuntimeException("❌ Danh sách món không được rỗng!");
//         }

//         // ✅ 2. TÌM ĐƠN HÀNG
//         Order order = repo.findById(orderId)
//                 .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy đơn hàng #" + orderId));

//         System.out.println("\n📋 Thông tin đơn hàng hiện tại:");
//         System.out.println("   - ID: " + order.getId());
//         System.out.println("   - Trạng thái: " + order.getStatus());
//         System.out.println("   - Bàn: " + (order.getTable() != null ? order.getTable().getNumber() : "N/A"));
//         System.out.println("   - Tổng tiền hiện tại: " + order.getTotalAmount() + "₫");

//         // ✅ 3. KIỂM TRA TRẠNG THÁI ĐƠN HÀNG
//         if ("PAID".equals(order.getStatus())) {
//             throw new RuntimeException("❌ Không thể thêm món vào đơn hàng đã thanh toán!");
//         }
//         if ("CANCELLED".equals(order.getStatus())) {
//             throw new RuntimeException("❌ Không thể thêm món vào đơn hàng đã hủy!");
//         }

//         // ✅ 4. LẤY TỔNG TIỀN HIỆN TẠI
//         BigDecimal currentTotal = order.getTotalAmount() != null
//                 ? order.getTotalAmount()
//                 : BigDecimal.ZERO;

//         BigDecimal additionalAmount = BigDecimal.ZERO;
//         int successCount = 0;

//         // ✅ 5. LOG CHI TIẾT TỪNG MÓN NHẬN ĐƯỢC
//         System.out.println("\n➕ Chi tiết các món nhận được từ frontend:");
//         System.out.println("----------------------------------------");
//         for (int i = 0; i < itemDTOs.size(); i++) {
//             OrderItemDTO dto = itemDTOs.get(i);
//             System.out.println("\n   📦 Món " + (i + 1) + ":");
//             System.out.println("      - productId: " + dto.getProductId());
//             System.out.println("      - quantity: " + dto.getQuantity());
//             System.out.println("      - price (từ frontend): " + dto.getPrice() + "₫");
//         }
//         System.out.println("----------------------------------------");

//         // ✅ 6. THÊM TỪNG MÓN VÀO ĐƠN HÀNG
//         System.out.println("\n➕ Bắt đầu xử lý thêm món:");
//         for (int i = 0; i < itemDTOs.size(); i++) {
//             OrderItemDTO dto = itemDTOs.get(i);

//             try {
//                 System.out.println("\n   🔄 Xử lý món " + (i + 1) + "...");

//                 // ✅ VALIDATE DTO
//                 if (dto.getProductId() == null) {
//                     throw new RuntimeException("Product ID không được null!");
//                 }
//                 if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
//                     throw new RuntimeException("Số lượng phải lớn hơn 0!");
//                 }

//                 // ✅ KIỂM TRA GIÁ TỪ FRONTEND
//                 if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
//                     throw new RuntimeException("Giá từ frontend không hợp lệ: " + dto.getPrice());
//                 }

//                 // ✅ LẤY THÔNG TIN SẢN PHẨM (CHỈ ĐỂ VERIFY)
//                 Product product = productRepo.findById(dto.getProductId())
//                         .orElseThrow(() -> new RuntimeException(
//                                 "Không tìm thấy sản phẩm #" + dto.getProductId()));

//                 System.out.println("      ✓ Tìm thấy sản phẩm: " + product.getName());
//                 System.out.println("      - Giá gốc (DB): " + product.getPrice() + "₫");
//                 System.out.println("      - Giá thực tế (Frontend): " + dto.getPrice() + "₫");

//                 // ✅ SO SÁNH GIÁ (CHỈ ĐỂ CẢNH BÁO, KHÔNG CHẶN)
//                 if (dto.getPrice().compareTo(product.getPrice()) > 0) {
//                     System.out.println("      ⚠️ CẢNH BÁO: Giá frontend CAO HƠN giá DB!");
//                 } else if (dto.getPrice().compareTo(product.getPrice()) < 0) {
//                     System.out.println("      ✓ Giá frontend THẤP HƠN giá DB (có khuyến mãi)");
//                 } else {
//                     System.out.println("      ✓ Giá frontend BẰNG giá DB (không khuyến mãi)");
//                 }

//                 // ✅ TẠO OrderItem với GIÁ TỪ FRONTEND
//                 OrderItem orderItem = new OrderItem();
//                 orderItem.setOrder(order);
//                 orderItem.setProduct(product);
//                 orderItem.setQuantity(dto.getQuantity());
//                 orderItem.setPrice(dto.getPrice());

//                 // ✅ TÍNH SUBTOTAL VỚI GIÁ ĐÃ GIẢM
//                 BigDecimal subtotal = dto.getPrice()
//                         .multiply(BigDecimal.valueOf(dto.getQuantity()));
//                 orderItem.setSubtotal(subtotal);

//                 // ✅ SET TIMESTAMP (nếu có)
//                 try {
//                     if (orderItem.getCreatedAt() == null) {
//                         orderItem.setCreatedAt(LocalDateTime.now());
//                     }
//                     orderItem.setUpdatedAt(LocalDateTime.now());
//                 } catch (Exception e) {
//                     // Bỏ qua nếu entity không có trường timestamp
//                 }

//                 // ✅ LƯU OrderItem VÀO DATABASE
//                 OrderItem saved = orderItemRepo.save(orderItem);

//                 // ✅ CỘNG DỒN VÀO TỔNG TIỀN
//                 additionalAmount = additionalAmount.add(subtotal);
//                 successCount++;

//                 System.out.println("      ✅ Đã lưu OrderItem #" + saved.getId());
//                 System.out.println("      💰 Subtotal: " + subtotal + "₫");

//             } catch (Exception e) {
//                 System.err.println("      ❌ Lỗi khi thêm món " + (i + 1) + ": " + e.getMessage());
//                 throw new RuntimeException("Lỗi khi thêm món: " + e.getMessage(), e);
//             }
//         }

//         System.out.println("\n----------------------------------------");
//         System.out.println("✅ Đã thêm thành công " + successCount + "/" + itemDTOs.size() + " món");

//         // ✅ 7. CẬP NHẬT TỔNG TIỀN CỦA ĐƠN HÀNG
//         BigDecimal newTotal = currentTotal.add(additionalAmount);
//         order.setTotalAmount(newTotal);

//         // ✅ 8. LƯU LẠI ĐƠN HÀNG
//         Order updatedOrder = repo.save(order);

//         // ✅ 9. CẬP NHẬT BILL
//         System.out.println("\n💳 Đang cập nhật Bill...");

//         Optional<Bill> billOptional = billRepo.findByOrderId(orderId);

//         if (billOptional.isPresent()) {
//             Bill bill = billOptional.get();
//             BigDecimal oldBillTotal = bill.getTotalAmount();

//             bill.setTotalAmount(newTotal);
//             bill.setUpdatedAt(LocalDateTime.now());

//             billRepo.save(bill);

//             System.out.println("   ✅ Đã cập nhật Bill #" + bill.getId());
//             System.out.println("   - Tổng tiền cũ: " + oldBillTotal + "₫");
//             System.out.println("   - Tổng tiền mới: " + newTotal + "₫");
//         } else {
//             System.out.println("   ⚠️ Không tìm thấy Bill cho đơn #" + orderId);
//             System.out.println("   (Bill có thể chưa được tạo)");
//         }

//         // ✅ 10. IN KẾT QUẢ
//         System.out.println("\n💰 Kết quả tính toán:");
//         System.out.println("   - Tổng tiền cũ (Order): " + currentTotal + "₫");
//         System.out.println("   - Tiền món thêm (đã áp dụng khuyến mãi): " + additionalAmount + "₫");
//         System.out.println("   - Tổng tiền mới (Order): " + newTotal + "₫");
//         System.out.println("\n✅ Hoàn thành cập nhật đơn #" + orderId);
//         System.out.println("==========================================\n");

//         return updatedOrder;
//     }

//     // ==========================================
//     // ✅ MỚI 1: PHÂN TRANG
//     // ==========================================
//     @Override
//     public PageResponse<Order> getOrdersPaged(
//             String keyword,
//             OrderStatus status,
//             LocalDateTime fromDate,
//             LocalDateTime toDate,
//             Long tableId,
//             Pageable pageable
//     ) {
//         // Chuẩn hoá keyword: blank → null để query bỏ qua điều kiện
//         String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

//         Page<Order> page = repo.searchOrders(kw, status, fromDate, toDate, tableId, pageable);
//         return PageResponse.from(page, o -> o);
//     }

//     // ==========================================
//     // ✅ MỚI 2: THỐNG KÊ
//     // ==========================================
//     @Override
//     public Map<String, Object> getOrderStats() {
//         return repo.getOrderStats();
//     }
// }
























package com.example.cafe.security.services.impl;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Order;
import com.example.cafe.entity.OrderItem;
import com.example.cafe.entity.Product;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import com.example.cafe.entity.enums.OrderStatus;
import com.example.cafe.entity.Bill;
import com.example.cafe.repository.OrderRepository;
import com.example.cafe.repository.ProductRepository;
import com.example.cafe.repository.TableRepository;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.repository.OrderItemRepository;
import com.example.cafe.repository.BillRepository;
import com.example.cafe.security.services.OrderService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final TableRepository tableRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;
    private final BillRepository billRepo;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    public OrderServiceImpl(
            OrderRepository repo,
            TableRepository tableRepo,
            UserRepository userRepo,
            ProductRepository productRepo,
            OrderItemRepository orderItemRepo,
            BillRepository billRepo,
            ActivityLogService activityLogService) {      // 👈 MỚI
        this.repo = repo;
        this.tableRepo = tableRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.orderItemRepo = orderItemRepo;
        this.billRepo = billRepo;
        this.activityLogService = activityLogService;  // 👈 MỚI
    }

    // ==========================================
    // ✅ SAVE — TẠO ĐƠN HÀNG MỚI (có log)
    // ==========================================
    @Override
    public Order save(Order o) {
        if (o.getTable() != null && o.getTable().getId() != null) {
            tableRepo.findById(o.getTable().getId()).ifPresent(o::setTable);
        }
        if (o.getEmployee() != null && o.getEmployee().getId() != null) {
            userRepo.findById(o.getEmployee().getId()).ifPresent(o::setEmployee);
        }

        if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (var item : o.getOrderItems()) {
                if (item.getProduct() != null && item.getProduct().getId() != null) {
                    Product p = productRepo.findById(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    item.setProduct(p);
                    item.setPrice(p.getPrice());
                    item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                total = total.add(item.getSubtotal());
            }
            o.setTotalAmount(total);
        }

        Order saved = repo.save(o);

        // ✅ GHI LOG
        try {
            String tableInfo = saved.getTable() != null
                    ? "bàn số " + saved.getTable().getNumber()
                    : "mang về";
            int itemCount = saved.getOrderItems() != null ? saved.getOrderItems().size() : 0;

            activityLogService.logCreate(
                    "Đơn hàng",
                    "#DH-" + saved.getId(),
                    "Tạo đơn hàng mới",
                    String.format("Đơn #%d tại %s — %d món, tổng %,.0fđ",
                            saved.getId(),
                            tableInfo,
                            itemCount,
                            saved.getTotalAmount() != null ? saved.getTotalAmount() : BigDecimal.ZERO)
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE order: " + e.getMessage());
        }

        return saved;
    }

    // ==========================================
    // ✅ UPDATE — giữ nguyên
    // ==========================================
    @Override
    public Order update(Long id, Order o) {
        return repo.findById(id).map(existing -> {
            existing.setStatus(o.getStatus());
            existing.setNotes(o.getNotes());
            existing.setPromotion(o.getPromotion());
            existing.setTotalAmount(o.getTotalAmount());

            if (o.getTable() != null && o.getTable().getId() != null) {
                tableRepo.findById(o.getTable().getId()).ifPresent(existing::setTable);
            }

            if (o.getEmployee() != null && o.getEmployee().getId() != null) {
                userRepo.findById(o.getEmployee().getId()).ifPresent(existing::setEmployee);
            }

            if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
                BigDecimal total = BigDecimal.ZERO;
                for (var item : o.getOrderItems()) {
                    if (item.getProduct() != null && item.getProduct().getId() != null) {
                        Product p = productRepo.findById(item.getProduct().getId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                        item.setProduct(p);
                        item.setPrice(p.getPrice());
                        item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                    total = total.add(item.getSubtotal());
                }
                existing.setOrderItems(o.getOrderItems());
                existing.setTotalAmount(total);
            }

            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // ==========================================
    // ✅ DELETE — có log
    // ==========================================
    @Override
    public void delete(Long id) {
        Order order = repo.findById(id).orElse(null);
        if (order == null) {
            repo.deleteById(id);
            return;
        }

        String tableInfo = order.getTable() != null
                ? "bàn " + order.getTable().getNumber()
                : "mang về";
        BigDecimal total = order.getTotalAmount();

        repo.deleteById(id);

        // ✅ GHI LOG
        try {
            activityLogService.logDelete(
                    "Đơn hàng",
                    "#DH-" + id,
                    "Xóa đơn hàng",
                    String.format("Đã xóa đơn #%d (%s, tổng %,.0fđ) khỏi hệ thống",
                            id, tableInfo, total != null ? total : BigDecimal.ZERO)
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE order: " + e.getMessage());
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return repo.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    // ==========================================
    // ✅ UPDATE STATUS — có log
    // ==========================================
    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = repo.save(order);

        System.out.println("💾 Đã lưu trạng thái mới vào database:");
        System.out.println("   - Order ID: " + id);
        System.out.println("   - New Status: " + status);

        // ✅ GHI LOG
        try {
            // PAID → SUCCESS, CANCELLED → WARNING, còn lại → INFO
            LogLevel level = LogLevel.INFO;
            if (status == OrderStatus.PAID) level = LogLevel.SUCCESS;
            else if (status == OrderStatus.CANCELLED) level = LogLevel.WARNING;

            String tableInfo = savedOrder.getTable() != null
                    ? "bàn " + savedOrder.getTable().getNumber()
                    : "mang về";

            activityLogService.saveLog(
                    LogAction.UPDATE,
                    level,
                    "Đơn hàng",
                    "#DH-" + id,
                    "Cập nhật trạng thái đơn hàng",
                    String.format("Đơn #%d (%s): %s → %s",
                            id, tableInfo, oldStatus, status)
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log update order status: " + e.getMessage());
        }

        return savedOrder;
    }

    // ==========================================
    // ✅ ADD ITEMS — giữ nguyên + có log
    // ==========================================
    @Override
    @Transactional
    public Order addItemsToOrder(Long orderId, List<OrderItemDTO> itemDTOs) {
        System.out.println("\n🔄 ==========================================");
        System.out.println("🔄 BACKEND: addItemsToOrder");
        System.out.println("🔄 ==========================================");
        System.out.println("📦 Order ID: " + orderId);
        System.out.println("📦 Số món nhận được: " + itemDTOs.size());

        if (itemDTOs == null || itemDTOs.isEmpty()) {
            throw new RuntimeException("❌ Danh sách món không được rỗng!");
        }

        Order order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy đơn hàng #" + orderId));

        System.out.println("\n📋 Thông tin đơn hàng hiện tại:");
        System.out.println("   - ID: " + order.getId());
        System.out.println("   - Trạng thái: " + order.getStatus());
        System.out.println("   - Bàn: " + (order.getTable() != null ? order.getTable().getNumber() : "N/A"));
        System.out.println("   - Tổng tiền hiện tại: " + order.getTotalAmount() + "₫");

        if ("PAID".equals(order.getStatus())) {
            throw new RuntimeException("❌ Không thể thêm món vào đơn hàng đã thanh toán!");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("❌ Không thể thêm món vào đơn hàng đã hủy!");
        }

        BigDecimal currentTotal = order.getTotalAmount() != null
                ? order.getTotalAmount()
                : BigDecimal.ZERO;

        BigDecimal additionalAmount = BigDecimal.ZERO;
        int successCount = 0;

        System.out.println("\n➕ Chi tiết các món nhận được từ frontend:");
        System.out.println("----------------------------------------");
        for (int i = 0; i < itemDTOs.size(); i++) {
            OrderItemDTO dto = itemDTOs.get(i);
            System.out.println("\n   📦 Món " + (i + 1) + ":");
            System.out.println("      - productId: " + dto.getProductId());
            System.out.println("      - quantity: " + dto.getQuantity());
            System.out.println("      - price (từ frontend): " + dto.getPrice() + "₫");
        }
        System.out.println("----------------------------------------");

        System.out.println("\n➕ Bắt đầu xử lý thêm món:");
        for (int i = 0; i < itemDTOs.size(); i++) {
            OrderItemDTO dto = itemDTOs.get(i);
            try {
                System.out.println("\n   🔄 Xử lý món " + (i + 1) + "...");

                if (dto.getProductId() == null) {
                    throw new RuntimeException("Product ID không được null!");
                }
                if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                    throw new RuntimeException("Số lượng phải lớn hơn 0!");
                }
                if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Giá từ frontend không hợp lệ: " + dto.getPrice());
                }

                Product product = productRepo.findById(dto.getProductId())
                        .orElseThrow(() -> new RuntimeException(
                                "Không tìm thấy sản phẩm #" + dto.getProductId()));

                System.out.println("      ✓ Tìm thấy sản phẩm: " + product.getName());
                System.out.println("      - Giá gốc (DB): " + product.getPrice() + "₫");
                System.out.println("      - Giá thực tế (Frontend): " + dto.getPrice() + "₫");

                if (dto.getPrice().compareTo(product.getPrice()) > 0) {
                    System.out.println("      ⚠️ CẢNH BÁO: Giá frontend CAO HƠN giá DB!");
                } else if (dto.getPrice().compareTo(product.getPrice()) < 0) {
                    System.out.println("      ✓ Giá frontend THẤP HƠN giá DB (có khuyến mãi)");
                } else {
                    System.out.println("      ✓ Giá frontend BẰNG giá DB (không khuyến mãi)");
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(dto.getQuantity());
                orderItem.setPrice(dto.getPrice());

                BigDecimal subtotal = dto.getPrice()
                        .multiply(BigDecimal.valueOf(dto.getQuantity()));
                orderItem.setSubtotal(subtotal);

                try {
                    if (orderItem.getCreatedAt() == null) {
                        orderItem.setCreatedAt(LocalDateTime.now());
                    }
                    orderItem.setUpdatedAt(LocalDateTime.now());
                } catch (Exception e) {
                    // Bỏ qua
                }

                OrderItem saved = orderItemRepo.save(orderItem);

                additionalAmount = additionalAmount.add(subtotal);
                successCount++;

                System.out.println("      ✅ Đã lưu OrderItem #" + saved.getId());
                System.out.println("      💰 Subtotal: " + subtotal + "₫");

            } catch (Exception e) {
                System.err.println("      ❌ Lỗi khi thêm món " + (i + 1) + ": " + e.getMessage());
                throw new RuntimeException("Lỗi khi thêm món: " + e.getMessage(), e);
            }
        }

        System.out.println("\n----------------------------------------");
        System.out.println("✅ Đã thêm thành công " + successCount + "/" + itemDTOs.size() + " món");

        BigDecimal newTotal = currentTotal.add(additionalAmount);
        order.setTotalAmount(newTotal);

        Order updatedOrder = repo.save(order);

        System.out.println("\n💳 Đang cập nhật Bill...");
        Optional<Bill> billOptional = billRepo.findByOrderId(orderId);

        if (billOptional.isPresent()) {
            Bill bill = billOptional.get();
            BigDecimal oldBillTotal = bill.getTotalAmount();
            bill.setTotalAmount(newTotal);
            bill.setUpdatedAt(LocalDateTime.now());
            billRepo.save(bill);

            System.out.println("   ✅ Đã cập nhật Bill #" + bill.getId());
            System.out.println("   - Tổng tiền cũ: " + oldBillTotal + "₫");
            System.out.println("   - Tổng tiền mới: " + newTotal + "₫");
        } else {
            System.out.println("   ⚠️ Không tìm thấy Bill cho đơn #" + orderId);
        }

        // ✅ GHI LOG
        try {
            activityLogService.saveLog(
                    LogAction.UPDATE,
                    LogLevel.INFO,
                    "Đơn hàng",
                    "#DH-" + orderId,
                    "Thêm món vào đơn hàng",
                    String.format("Đơn #%d: thêm %d món (%,.0fđ). Tổng mới: %,.0fđ",
                            orderId, successCount, additionalAmount, newTotal)
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log add items: " + e.getMessage());
        }

        System.out.println("\n💰 Kết quả tính toán:");
        System.out.println("   - Tổng tiền cũ (Order): " + currentTotal + "₫");
        System.out.println("   - Tiền món thêm: " + additionalAmount + "₫");
        System.out.println("   - Tổng tiền mới (Order): " + newTotal + "₫");
        System.out.println("\n✅ Hoàn thành cập nhật đơn #" + orderId);
        System.out.println("==========================================\n");

        return updatedOrder;
    }

    @Override
    public PageResponse<Order> getOrdersPaged(
            String keyword,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long tableId,
            Pageable pageable
    ) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Order> page = repo.searchOrders(kw, status, fromDate, toDate, tableId, pageable);
        return PageResponse.from(page, o -> o);
    }

    @Override
    public Map<String, Object> getOrderStats() {
        return repo.getOrderStats();
    }
}