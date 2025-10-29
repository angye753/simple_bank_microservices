package rabobank.simple_bank_service.application.mappers;

import org.mapstruct.Mapper;
import rabobank.simple_bank_service.domain.model.CardDTO;
import rabobank.simple_bank_service.infrastructure.entities.Card;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardDTO toDTO(Card user);
}