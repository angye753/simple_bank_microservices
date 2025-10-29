package rabobank.simple_bank_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerEventDTO {
    private String eventType;
    private List<LedgerDTO> ledgerDTO;
}
