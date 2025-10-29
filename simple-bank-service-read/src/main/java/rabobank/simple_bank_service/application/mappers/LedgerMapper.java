package rabobank.simple_bank_service.application.mappers;

import org.mapstruct.Mapper;
import rabobank.simple_bank_service.domain.model.LedgerDTO;
import rabobank.simple_bank_service.infrastructure.entities.Ledger;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface LedgerMapper {

    LedgerDTO toDTO(Ledger ledger);
    Ledger toEntity(LedgerDTO ledgerDTO);
}
