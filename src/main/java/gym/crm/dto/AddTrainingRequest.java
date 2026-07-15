package gym.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AddTrainingRequest {
    @NotBlank(message = "traineeUsername is required")
    private String traineeUsername;

    @NotBlank(message = "trainerUsername is required")
    private String trainerUsername;

    @NotBlank(message = "trainingName is required")
    private String trainingName;

    @NotNull(message = "trainingDate is required")
    private LocalDate trainingDate;

    @Positive(message = "trainingDuration must be greater than 0")
    private int trainingDuration;
}
