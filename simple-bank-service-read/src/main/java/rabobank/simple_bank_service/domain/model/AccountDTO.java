package rabobank.simple_bank_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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

}

