package com.example.cafe.scheduler;

import com.example.cafe.entity.Order;
import com.example.cafe.entity.enums.OrderStatus; // ✅ thêm dòng này
import com.example.cafe.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Thời hạn cảnh báo (phút)
    private final long WARNING_MINUTES = 15;

    // Chạy mỗi 1 phút
    @Scheduled(fixedRate = 15000)
    public void checkPreparingOrders() {
        // ✅ dùng Enum thay vì String
        List<Order> preparing = orderRepository.findByStatus(OrderStatus.PREPARING);

        LocalDateTime now = LocalDateTime.now();

        for (Order o : preparing) {
            LocalDateTime updated = o.getUpdatedAt();
            if (updated == null) continue;

            long minutes = Duration.between(updated, now).toMinutes();

            if (minutes >= WARNING_MINUTES) {
                sendWarningToSocket(o);
            }
        }
    }

    private void sendWarningToSocket(Order order) {
        try {
            String url = "http://localhost:3001/order-warning"; // Socket server của bạn
            String message = "⚠️ Đơn #" + order.getId() + " (Bàn " +
                    (order.getTable() != null ? order.getTable().getNumber() : "N/A") +
                    ") đang ở trạng thái đang chuẩn bị quá " + WARNING_MINUTES + " phút.";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String payload = String.format("{\"orderId\": %d, \"message\": \"%s\"}",
                    order.getId(), message);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Đã gửi cảnh báo socket: " + message);

        } catch (Exception ex) {
            System.err.println("❌ Lỗi gửi cảnh báo tới socket: " + ex.getMessage());
        }
    }
}
