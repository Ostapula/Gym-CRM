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
public class TrainingTypeEntityDto {
    private Integer id;
    private TrainingType type;
}

