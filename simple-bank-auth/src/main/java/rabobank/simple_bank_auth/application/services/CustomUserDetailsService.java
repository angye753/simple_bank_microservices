package rabobank.simple_bank_auth.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import rabobank.simple_bank_auth.domain.model.UserDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Service
@Slf4j
public class CustomUserDetailsService  implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("load user by username {} " , username);
        UserDTO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}