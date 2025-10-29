package rabobank.simple_bank_service.infrastructure.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "app_user")
public class AppUser {

    @Id
    private String id;

    private String name;
    private String username;
    private String password;
    private String contactNumber;
    private String address;
    private String role;

}

