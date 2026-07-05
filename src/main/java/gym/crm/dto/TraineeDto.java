package gym.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraineeDto extends UserDto{

    private String address;
    private LocalDate dob;
    private Set<Long> trainings;
    private Set<Long> trainers;

    public TraineeDto(String firstName, String lastName, String username, String password, boolean active, String address, LocalDate dob, Set<Long> trainings, Set<Long> trainers) {
        super(firstName, lastName, username, password, active);
        this.address = address;
        this.dob = dob;
        this.trainings = trainings;
        this.trainers = trainers;
    }

    public TraineeDto(Long id, String firstName, String lastName, String username, String password, boolean active, String address, LocalDate dob, Set<Long> trainings, Set<Long> trainers) {
        super(id, firstName, lastName, username, password, active);
        this.address = address;
        this.dob = dob;
        this.trainings = trainings;
        this.trainers = trainers;
    }
}

