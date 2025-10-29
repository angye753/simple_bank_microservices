package rabobank.simple_bank_service.application.out.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import rabobank.simple_bank_service.domain.model.LedgerEventDTO;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(LedgerEventDTO eventDTO) {
        kafkaTemplate.send("ledger-event-topic", eventDTO);
    }
}