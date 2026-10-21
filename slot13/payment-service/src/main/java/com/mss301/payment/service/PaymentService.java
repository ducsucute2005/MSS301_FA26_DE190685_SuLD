package com.mss301.payment.service;

import com.mss301.payment.dto.PaymentRequest;
import com.mss301.payment.entity.*;
import com.mss301.payment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    // LOCAL TRANSACTION cua buoc 3: tru tien vi + ghi payment (cung paymentdb tren SQL Server)
    @Transactional
    public Payment pay(PaymentRequest req) {
        var existing = paymentRepository.findById(req.orderId());
        if (existing.isPresent()) {               // idempotent
            return existing.get();
        }

        Wallet wallet = walletRepository.findById(req.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Khong tim thay vi cua " + req.customerId()));

        if (wallet.getBalance() < req.amount()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "So du khong du: con " + wallet.getBalance() + ", can " + req.amount());
        }

        wallet.setBalance(wallet.getBalance() - req.amount());   // JPA dirty checking tu UPDATE

        Payment p = new Payment();
        p.setOrderId(req.orderId());
        p.setCustomerId(req.customerId());
        p.setAmount(req.amount());
        p.setStatus(PaymentStatus.PAID);
        log.info("PAY order={} customer={} amount={} -> balance con {}",
                req.orderId(), req.customerId(), req.amount(), wallet.getBalance());
        return paymentRepository.save(p);
    }

    // COMPENSATING TRANSACTION cua buoc 3: hoan tien
    @Transactional
    public void refund(Long orderId) {
        var opt = paymentRepository.findById(orderId);
        if (opt.isEmpty() || opt.get().getStatus() == PaymentStatus.REFUNDED) {
            log.info("REFUND order={} -> khong co gi de hoan tac", orderId);
            return;
        }
        Payment p = opt.get();
        Wallet wallet = walletRepository.findById(p.getCustomerId()).orElseThrow();
        wallet.setBalance(wallet.getBalance() + p.getAmount());
        p.setStatus(PaymentStatus.REFUNDED);
        log.info("REFUND order={} -> hoan {} cho {}, balance = {}",
                orderId, p.getAmount(), p.getCustomerId(), wallet.getBalance());
    }
}
