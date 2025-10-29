package rabobank.simple_bank_service.domain.model;

import lombok.*;
import rabobank.simple_bank_service.infrastructure.entities.CardType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDTO {
    private String cardNumber;
    private CardType cardType;
}
