package rabobank.simple_bank_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDTO {
    private String id;
    private UserDTO user;
    private BigDecimal balance;
    private String currency;
    private CardDTO cardDTO;

}

