package gym.crm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TraineeDto extends UserDto {

    private String address;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dob;
    private Set<Long> trainings;
    private Set<String> trainers;

    public TraineeDto(String firstName, String lastName, String username, String password, boolean active, String address, LocalDate dob, Set<Long> trainings, Set<String> trainers) {
        super(firstName, lastName, username, password, active);
        this.address = address;
        this.dob = dob;
        this.trainings = trainings;
        this.trainers = trainers;
    }
}

