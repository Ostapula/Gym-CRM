package gym.crm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Trainer extends User {
    private Long userId;
    private TrainingType specialization;

    public Trainer(String firstName, String lastName, String username, String password, boolean isActive, Long userId, TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.specialization = specialization;
    }
}
