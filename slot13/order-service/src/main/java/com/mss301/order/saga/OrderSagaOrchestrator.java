package com.mss301.order.saga;

import com.mss301.order.client.InventoryClient;
import com.mss301.order.client.PaymentClient;
import com.mss301.order.dto.CreateOrderRequest;
import com.mss301.order.entity.*;
import com.mss301.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// KHONG dat @Transactional o day: orchestrator goi qua mang sang service khac,
// moi buoc da la 1 local transaction rieng tren database rieng.
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final OrderService orderService;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    // Spring Framework 7: RetryTemplate co san trong spring-core (khong can thu vien spring-retry).
    // maxRetries(2) = 1 lan chay dau + 2 lan thu lai = toi da 3 lan, cach nhau 1 giay.
    private final RetryTemplate compensationRetry = new RetryTemplate(
            RetryPolicy.builder()
                    .includes(RestClientException.class)
                    .maxRetries(2)
                    .delay(Duration.ofSeconds(1))
                    .build());

    // Chi dung cho demo KB8 (mac dinh 0 = khong dung)
    @Value("${saga.demo.payment-delay-ms:0}")
    private long paymentDelayMs;

    public Order placeOrder(CreateOrderRequest req) {
        // ===== Buoc 1: tao order PENDING (local transaction o orderdb) =====
        Order order = orderService.createPendingOrder(req);
        Long orderId = order.getId();
        orderService.log(orderId, "CREATE_ORDER", "SUCCESS", "Order PENDING");
        orderService.updateSagaStatus(orderId, SagaStatus.EXECUTING);

        // Ngan xep cac compensating action cua nhung buoc DA thanh cong
        Deque<Runnable> compensations = new ArrayDeque<>();

        try {
            // ===== Buoc 2: giu hang (inventory-service / MongoDB) =====
            inventoryClient.reserve(orderId, req.productId(), req.quantity());
            orderService.log(orderId, "RESERVE_INVENTORY", "SUCCESS", "Da giu " + req.quantity() + " " + req.productId());
            compensations.push(() -> inventoryClient.release(orderId));

            if (paymentDelayMs > 0) {
                log.warn("[DEMO] Cho {} ms truoc khi thanh toan order={}", paymentDelayMs, orderId);
                sleep(paymentDelayMs);
            }

            // ===== Buoc 3: thanh toan (payment-service / SQL Server) =====
            paymentClient.pay(orderId, req.customerId(), req.amount());
            orderService.log(orderId, "PROCESS_PAYMENT", "SUCCESS", "Da thu " + req.amount());
            compensations.push(() -> paymentClient.refund(orderId));

            // ===== Buoc cuoi: xac nhan order =====
            orderService.confirmOrder(orderId);
            orderService.log(orderId, "CONFIRM_ORDER", "SUCCESS", "Saga COMPLETED");

        } catch (RestClientException ex) {
            String reason = extractReason(ex);
            log.warn("Saga order={} that bai: {}", orderId, reason);
            orderService.log(orderId, "SAGA", "FAILED", reason);
            orderService.markFailed(orderId, reason);
            compensate(orderId, compensations);
        }
        return orderService.findById(orderId);
    }

    // Chay compensation theo thu tu NGUOC (LIFO) roi huy order
    private void compensate(Long orderId, Deque<Runnable> compensations) {
        orderService.updateSagaStatus(orderId, SagaStatus.COMPENSATING);
        boolean allOk = true;
        while (!compensations.isEmpty()) {
            Runnable action = compensations.pop();
            allOk &= runWithRetry(orderId, action);
        }
        if (!allOk) {
            // Giu nguyen COMPENSATING de admin/job khac xu ly tiep, KHONG bao COMPENSATED gia
            log.error("Compensation cho order={} chua xong, can xu ly thu cong", orderId);
            return;
        }
        orderService.cancelOrder(orderId);          // compensation cua buoc 1
        orderService.log(orderId, "CANCEL_ORDER", "SUCCESS", "Saga COMPENSATED");
    }

    // Compensation phai "Reliable" -> thu lai toi da 3 lan (Slide 25)
    private boolean runWithRetry(Long orderId, Runnable action) {
        AtomicInteger attempt = new AtomicInteger();
        try {
            compensationRetry.execute(() -> {
                int n = attempt.incrementAndGet();
                try {
                    action.run();
                } catch (RestClientException ex) {
                    orderService.log(orderId, "COMPENSATION", "FAILED", "Lan thu " + n + ": " + ex.getMessage());
                    throw ex;                       // nem lai de RetryTemplate thu lan tiep theo
                }
                orderService.log(orderId, "COMPENSATION", "SUCCESS", "Lan thu " + n);
                return null;
            });
            return true;
        } catch (RetryException ex) {               // da het so lan thu
            return false;
        }
    }

    private String extractReason(RestClientException ex) {
        if (ex instanceof RestClientResponseException r) {
            try {
                Map<?, ?> body = r.getResponseBodyAs(Map.class);
                if (body != null && body.get("message") != null) {
                    return r.getStatusCode().value() + " - " + body.get("message");
                }
            } catch (Exception ignored) { }
            return r.getStatusCode().toString();
        }
        return "Service khong phan hoi: " + ex.getMessage();   // VD: container bi stop
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
