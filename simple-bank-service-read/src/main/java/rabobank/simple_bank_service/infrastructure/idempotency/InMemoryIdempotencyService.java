package rabobank.simple_bank_service.infrastructure.idempotency;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryIdempotencyService implements IdempotencyService {
    private final Set<String> keys = ConcurrentHashMap.newKeySet();
    @Override
    public boolean checkAndMark(String idempotencyKey) {
        if (idempotencyKey == null) return true;
        return keys.add(idempotencyKey);
    }
}

