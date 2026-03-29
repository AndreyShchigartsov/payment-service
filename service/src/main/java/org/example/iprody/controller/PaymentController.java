package org.example.iprody.controller;

import lombok.RequiredArgsConstructor;
import org.example.iprody.dto.PaymentRequest;
import org.example.iprody.dto.PaymentResponse;
import org.example.iprody.dto.PaymentStatusUpdateRequest;
import org.example.iprody.service.PaymentService;
import org.example.iprody.valueobject.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDoc {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        return new ResponseEntity<>(paymentService.createPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<PaymentResponse> getPaymentByNumber(@PathVariable String paymentNumber) {
        return ResponseEntity.ok(paymentService.getPaymentByNumber(paymentNumber));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/stats/count/{status}")
    public ResponseEntity<Long> getPaymentCountByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentCountByStatus(status));
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable UUID id,
                                                         @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(@PathVariable UUID id,
                                                               @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
