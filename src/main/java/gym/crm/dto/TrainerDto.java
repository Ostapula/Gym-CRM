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
public class TrainerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
    private Integer specializationId;
    private TrainingType specializationType;
    private Set<Long> traineeIds;
    private Set<Long> trainingIds;
}

