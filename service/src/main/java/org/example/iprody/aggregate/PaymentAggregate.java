package org.example.iprody.aggregate;

import lombok.Getter;
import org.example.iprody.entity.Payment;
import org.example.iprody.valueobject.PaymentStatus;

import java.time.LocalDateTime;

@Getter
public class PaymentAggregate {
    private final Payment payment;
    private boolean canBeProcessed;
    private boolean canBeRefunded;
    private boolean canBeCancelled;

    public PaymentAggregate(Payment payment) {
        this.payment = payment;
        validateState();
    }

    private void validateState() {
        this.canBeProcessed = payment.getStatus() == PaymentStatus.PENDING;
        this.canBeRefunded = payment.getStatus() == PaymentStatus.COMPLETED;
        this.canBeCancelled = payment.getStatus() == PaymentStatus.PENDING ||
                payment.getStatus() == PaymentStatus.PROCESSING;
    }

    public PaymentAggregate processPayment(String transactionId) {
        if (!canBeProcessed) {
            throw new IllegalStateException("Payment cannot be processed. Current status: " +
                    payment.getStatus());
        }

        Payment processedPayment = Payment.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .status(PaymentStatus.COMPLETED)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(transactionId)
                .paymentDetails(payment.getPaymentDetails())
                .payerId(payment.getPayerId())
                .payerEmail(payment.getPayerEmail())
                .completedAt(LocalDateTime.now())
                .build();

        return new PaymentAggregate(processedPayment);
    }

    public PaymentAggregate failPayment(String errorMessage) {
        Payment failedPayment = Payment.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .status(PaymentStatus.FAILED)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDetails(payment.getPaymentDetails())
                .payerId(payment.getPayerId())
                .payerEmail(payment.getPayerEmail())
                .errorMessage(errorMessage)
                .build();

        return new PaymentAggregate(failedPayment);
    }

    public PaymentAggregate refundPayment() {
        if (!canBeRefunded) {
            throw new IllegalStateException("Payment cannot be refunded");
        }

        Payment refundedPayment = Payment.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .status(PaymentStatus.REFUNDED)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDetails(payment.getPaymentDetails())
                .payerId(payment.getPayerId())
                .payerEmail(payment.getPayerEmail())
                .build();

        return new PaymentAggregate(refundedPayment);
    }

    public PaymentAggregate cancelPayment() {
        if (!canBeCancelled) {
            throw new IllegalStateException("Payment cannot be cancelled");
        }

        Payment cancelledPayment = Payment.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .status(PaymentStatus.CANCELLED)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDetails(payment.getPaymentDetails())
                .payerId(payment.getPayerId())
                .payerEmail(payment.getPayerEmail())
                .build();

        return new PaymentAggregate(cancelledPayment);
    }
}