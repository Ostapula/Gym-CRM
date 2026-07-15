package gym.crm.dto;

import gym.crm.validation.OnCreate;
import gym.crm.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "first name is required", groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(message = "last name is required", groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @NotBlank(message = "username is required", groups = OnUpdate.class)
    private String username;

    private String password;
    private boolean active;

    public UserDto(String firstName, String lastName, String username, String password, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.active = active;
    }
}
