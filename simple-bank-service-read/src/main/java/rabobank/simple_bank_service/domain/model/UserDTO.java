package rabobank.simple_bank_service.domain.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private String id;
    private String name;
    private String username;
    private String contactNumber;
    private String address;
    private String password;
    private String role;
}
