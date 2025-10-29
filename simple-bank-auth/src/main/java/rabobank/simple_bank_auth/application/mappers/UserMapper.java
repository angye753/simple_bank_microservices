package rabobank.simple_bank_auth.application.mappers;

import org.mapstruct.Mapper;
import rabobank.simple_bank_auth.domain.model.UserDTO;
import rabobank.simple_bank_auth.infrastructure.entities.AppUser;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(AppUser user);
}
