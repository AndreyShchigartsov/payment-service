package org.example.iprody.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.iprody.enums.IdempotencyKeyStatus;

import java.time.ZonedDateTime;

@Entity
@Table(name = "idempotency_keys")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
@ToString
@Getter
@Setter
public class IdempotencyKey {

    @Id
    @Column(name = "key_value")
    private String key;

    @Enumerated(EnumType.STRING)
    private IdempotencyKeyStatus status;

    @Lob
    private String response;

    private ZonedDateTime createdAt;

    private int statusCode;

    public IdempotencyKey(String key, IdempotencyKeyStatus status) {
        this.key = key;
        this.status = status;
        this.createdAt = ZonedDateTime.now();
    }
}
