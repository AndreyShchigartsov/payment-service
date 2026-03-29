package org.example.iprody.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IdempotencyKeyStatus {

    PENDING, COMPLETED

}
