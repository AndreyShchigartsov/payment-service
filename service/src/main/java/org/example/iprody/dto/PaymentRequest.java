package org.example.iprody.dto;

import lombok.Data;
import org.example.iprody.valueobject.Money;
import org.example.iprody.valueobject.PaymentMethod;

@Data
public class PaymentRequest {
    private String orderId;
    private Money amount;
    private PaymentMethod paymentMethod;
    private String paymentDetails;
    private String payerId;
    private String payerEmail;
}
