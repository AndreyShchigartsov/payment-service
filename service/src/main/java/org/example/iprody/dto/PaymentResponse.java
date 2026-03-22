package org.example.iprody.dto;

import lombok.Builder;
import lombok.Data;
import org.example.iprody.entity.Payment;
import org.example.iprody.valueobject.Money;
import org.example.iprody.valueobject.PaymentMethod;
import org.example.iprody.valueobject.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private String paymentNumber;
    private String orderId;
    private PaymentStatus status;
    private Money amount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private String paymentDetails;
    private String payerId;
    private String payerEmail;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDetails(payment.getPaymentDetails())
                .payerId(payment.getPayerId())
                .payerEmail(payment.getPayerEmail())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
