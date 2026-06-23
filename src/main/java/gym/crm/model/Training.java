package gym.crm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Training {
    private Long id;
    private Long trainerId;
    private Long traineeId;
    private String name;
    private TrainingType type;
    private LocalDate date;
    private int duration;
}
