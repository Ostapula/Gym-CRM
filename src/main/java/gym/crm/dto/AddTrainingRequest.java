package gym.crm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AddTrainingRequest {
    private String traineeUsername;
    private String trainerUsername;
    private String trainingName;
    private LocalDate trainingDate;
    private int trainingDuration;
}
