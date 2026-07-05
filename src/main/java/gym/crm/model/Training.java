package gym.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "trainings")
@NamedQuery(name = "Training.findAll", query = "FROM Training")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainee_id")
    private Trainee trainee;
    @Column
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "training_type_id")
    private TrainingTypeEntity trainingType;
    @Column
    private LocalDate date;
    @Column
    private int duration;
}
