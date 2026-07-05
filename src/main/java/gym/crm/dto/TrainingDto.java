package gym.crm.dto;

import gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDto {
    private Long id;
    private Long trainerId;
    private Long traineeId;
    private String name;
    private Integer trainingTypeId;
    private TrainingType trainingType;
    private LocalDate date;
    private int duration;
}

