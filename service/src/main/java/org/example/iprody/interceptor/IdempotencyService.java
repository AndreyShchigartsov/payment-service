package org.example.iprody.interceptor;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.iprody.entity.IdempotencyKey;
import org.example.iprody.enums.IdempotencyKeyStatus;
import org.example.iprody.repository.IdempotencyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository repository;

    @Transactional
    public void createPendingKey(String key) {
        var newKey = new IdempotencyKey(key, IdempotencyKeyStatus.PENDING);
        try {
            repository.save(newKey);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyKeyExistsException("Key already exists", e);
        }
    }

    public Optional<IdempotencyKey> getByKey(String key) {
        return repository.findById(key);
    }

    @Transactional
    public void markAsCompleted(String key, String responseData, int statusCode) {
        var keyEntity = getByKey(key).orElseThrow(() -> new EntityNotFoundException("Key not found"));
        keyEntity.setStatus(IdempotencyKeyStatus.COMPLETED);
        keyEntity.setStatusCode(statusCode);
        keyEntity.setResponse(responseData);
        repository.save(keyEntity);
    }
}
