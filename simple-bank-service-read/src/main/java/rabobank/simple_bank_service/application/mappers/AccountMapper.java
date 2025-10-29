package rabobank.simple_bank_service.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rabobank.simple_bank_service.domain.model.AccountDTO;
import rabobank.simple_bank_service.infrastructure.entities.Account;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CardMapper.class})
public interface AccountMapper {
    @Mapping(source = "appUser", target = "user")
    @Mapping(source = "card", target = "cardDTO")
    AccountDTO toDTO(Account account);
    @Mapping(source = "user", target = "appUser")
    @Mapping(source = "cardDTO", target = "card")
    Account toEntity(AccountDTO account);
}
