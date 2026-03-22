package org.example.iprody.dto;

import lombok.Data;
import org.example.iprody.valueobject.PaymentStatus;

@Data
public class PaymentStatusUpdateRequest {
    private PaymentStatus status;
    private String transactionId;
    private String errorMessage;
}
