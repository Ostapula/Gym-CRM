package gym.crm.dto;

import gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummaryDto {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
}
