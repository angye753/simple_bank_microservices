package rabobank.simple_bank_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rabobank.simple_bank_service.infrastructure.entities.CardType;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDTO {
    private String id;
    private UserDTO user;
    private double balance;
    private String currency;
    private CardDTO cardDTO;


    public void withdraw(double amount) {
        double total = cardDTO.getCardType().equals(CardType.CREDIT) ? amount * 1.01 : amount;
        if (balance - total < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        setBalance(balance - total);
    }


    public void deposit(double amount) {
        balance += amount;
    }
}

