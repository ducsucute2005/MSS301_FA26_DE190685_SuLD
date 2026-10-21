package com.mss301.inventory.service;

import com.mss301.inventory.dto.ReserveRequest;
import com.mss301.inventory.entity.*;
import com.mss301.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    // LOCAL TRANSACTION cua buoc 2: tru kho + ghi reservation
    // (2 collection trong cung inventorydb -> MongoDB multi-document transaction)
    @Transactional
    public Reservation reserve(ReserveRequest req) {
        // Idempotent: neu orderId nay da duoc reserve roi thi tra lai ket qua cu
        var existing = reservationRepository.findById(req.orderId());
        if (existing.isPresent()) {
            log.info("Order {} da duoc reserve truoc do -> bo qua", req.orderId());
            return existing.get();
        }

        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Khong tim thay san pham " + req.productId()));

        if (product.getStock() < req.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Khong du hang: con " + product.getStock() + ", can " + req.quantity());
        }

        product.setStock(product.getStock() - req.quantity());
        productRepository.save(product);   // MongoDB KHONG co dirty checking nhu JPA -> phai save

        Reservation r = new Reservation();
        r.setOrderId(req.orderId());
        r.setProductId(req.productId());
        r.setQuantity(req.quantity());
        r.setStatus(ReservationStatus.RESERVED);
        log.info("RESERVE order={} product={} qty={} -> stock con {}",
                req.orderId(), req.productId(), req.quantity(), product.getStock());
        return reservationRepository.save(r);
    }

    // COMPENSATING TRANSACTION cua buoc 2: cong lai kho
    @Transactional
    public void release(Long orderId) {
        var opt = reservationRepository.findById(orderId);
        if (opt.isEmpty() || opt.get().getStatus() == ReservationStatus.RELEASED) {
            // Idempotent: chua reserve hoac da release roi -> khong lam gi
            log.info("RELEASE order={} -> khong co gi de hoan tac", orderId);
            return;
        }
        Reservation r = opt.get();
        Product product = productRepository.findById(r.getProductId()).orElseThrow();
        product.setStock(product.getStock() + r.getQuantity());
        r.setStatus(ReservationStatus.RELEASED);
        productRepository.save(product);
        reservationRepository.save(r);
        log.info("RELEASE order={} -> tra lai {} {}, stock = {}",
                orderId, r.getQuantity(), product.getId(), product.getStock());
    }
}
