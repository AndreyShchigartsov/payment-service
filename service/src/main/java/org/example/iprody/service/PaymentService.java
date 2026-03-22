package org.example.iprody.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.iprody.aggregate.PaymentAggregate;
import org.example.iprody.dto.PaymentRequest;
import org.example.iprody.dto.PaymentResponse;
import org.example.iprody.dto.PaymentStatusUpdateRequest;
import org.example.iprody.entity.Payment;
import org.example.iprody.repository.PaymentRepository;
import org.example.iprody.valueobject.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Проверка на существующий платеж по заказу
        if (paymentRepository.existsByOrderIdAndStatus(request.getOrderId(), PaymentStatus.PENDING)) {
            throw new RuntimeException("Payment already exists for order: " + request.getOrderId());
        }

        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .orderId(request.getOrderId())
                .status(PaymentStatus.PENDING)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDetails(request.getPaymentDetails())
                .payerId(request.getPayerId())
                .payerEmail(request.getPayerEmail())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: {} for order: {}", savedPayment.getPaymentNumber(), request.getOrderId());

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found with number: " + paymentNumber));
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(String orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse updatePayment(UUID id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Payment updatedPayment = Payment.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(request.getOrderId() != null ? request.getOrderId() : payment.getOrderId())
                .status(payment.getStatus())
                .amount(request.getAmount() != null ? request.getAmount() : payment.getAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : payment.getPaymentMethod())
                .paymentDetails(request.getPaymentDetails() != null ? request.getPaymentDetails() : payment.getPaymentDetails())
                .payerId(request.getPayerId() != null ? request.getPayerId() : payment.getPayerId())
                .payerEmail(request.getPayerEmail() != null ? request.getPayerEmail() : payment.getPayerEmail())
                .transactionId(payment.getTransactionId())
                .build();

        Payment savedPayment = paymentRepository.save(updatedPayment);
        log.info("Payment updated: {}", savedPayment.getPaymentNumber());

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(UUID id, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentAggregate aggregate = new PaymentAggregate(payment);
        PaymentAggregate updatedAggregate;

        switch (request.getStatus()) {
            case COMPLETED:
                updatedAggregate = aggregate.processPayment(request.getTransactionId());
                log.info("Payment completed: {} with transaction: {}",
                        payment.getPaymentNumber(), request.getTransactionId());
                break;

            case FAILED:
                updatedAggregate = aggregate.failPayment(request.getErrorMessage());
                log.error("Payment failed: {} - {}",
                        payment.getPaymentNumber(), request.getErrorMessage());
                break;

            case REFUNDED:
                updatedAggregate = aggregate.refundPayment();
                log.info("Payment refunded: {}", payment.getPaymentNumber());
                break;

            case CANCELLED:
                updatedAggregate = aggregate.cancelPayment();
                log.info("Payment cancelled: {}", payment.getPaymentNumber());
                break;

            default:
                throw new IllegalArgumentException("Cannot update to status: " + request.getStatus());
        }

        Payment savedPayment = paymentRepository.save(updatedAggregate.getPayment());
        return PaymentResponse.fromEntity(savedPayment);
    }

    @Transactional
    public void deletePayment(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
        log.info("Payment deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getPaymentCountByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        return paymentRepository.getTotalCompletedPayments();
    }

    private String generatePaymentNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
