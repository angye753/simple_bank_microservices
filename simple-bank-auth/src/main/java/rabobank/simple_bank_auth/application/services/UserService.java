package rabobank.simple_bank_auth.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rabobank.simple_bank_auth.domain.model.UserDTO;
import rabobank.simple_bank_auth.application.mappers.UserMapper;
import rabobank.simple_bank_auth.infrastructure.entities.AppUser;
import rabobank.simple_bank_auth.infrastructure.repositories.AppUserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;

    @Autowired
    private final UserMapper userMapper;

    public UserDTO getUserByUsername(String id) {
        AppUser user = appUserRepository.findByUsername(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return userMapper.toDTO(user);
    }

}
