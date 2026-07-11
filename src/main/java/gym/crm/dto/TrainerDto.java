package gym.crm.dto;

import gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDto extends UserDto {
    private Integer specializationId;
    private TrainingType specializationType;
    private Set<Long> traineeIds;
    private Set<Long> trainingIds;

    public TrainerDto(Long id, String firstName, String lastName, String username, String password, boolean active, Integer specializationId, TrainingType specializationType, Set<Long> traineeIds, Set<Long> trainingIds) {
        super(id, firstName, lastName, username, password, active);
        this.specializationId = specializationId;
        this.specializationType = specializationType;
        this.traineeIds = traineeIds;
        this.trainingIds = trainingIds;
    }
}

