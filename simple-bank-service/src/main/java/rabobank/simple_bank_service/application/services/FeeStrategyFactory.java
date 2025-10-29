package rabobank.simple_bank_service.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rabobank.simple_bank_service.infrastructure.entities.CardType;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FeeStrategyFactory {

    private final Map<String, FeeStrategy> strategies;

    public FeeStrategy getStrategy(CardType type) {
        String key = type.name().toLowerCase();
        FeeStrategy strategy = strategies.get(key);
        if (strategy == null)
            throw new IllegalArgumentException("No strategy found for card type: " + type);
        return strategy;
    }
}
