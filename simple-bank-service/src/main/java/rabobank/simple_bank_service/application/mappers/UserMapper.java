package rabobank.simple_bank_service.application.mappers;

import org.mapstruct.Mapper;
import rabobank.simple_bank_service.domain.model.UserDTO;
import rabobank.simple_bank_service.infrastructure.entities.AppUser;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(AppUser user);
}
