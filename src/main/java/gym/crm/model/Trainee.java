package gym.crm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Setter
@Getter
@ToString
public class Trainee extends User {
    private Long userId;
    private String address;
    private LocalDate dob;

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive, Long userId, String address, LocalDate dob) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.address = address;
        this.dob = dob;
    }
}
