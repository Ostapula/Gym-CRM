package gym.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@ToString
@Entity
@Table(name = "trainers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@PrimaryKeyJoinColumn(name = "id")
public class Trainer extends User {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id")
    private TrainingTypeEntity specialization;
    @ManyToMany(mappedBy = "trainers")
    @ToString.Exclude
    private Set<Trainee> trainees;
    @OneToMany(mappedBy = "trainer")
    @ToString.Exclude
    private Set<Training> trainings;

    public Trainer(String firstName, String lastName, String username, String password, boolean isActive, TrainingTypeEntity specialization) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
    }
}
