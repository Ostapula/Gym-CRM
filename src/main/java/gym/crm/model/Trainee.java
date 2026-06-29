package gym.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "trainees")
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Trainee extends User {
    @Column
    private String address;
    @Column
    private LocalDate dob;
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Training> trainings;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers;

    public Trainee(Long id, String firstName, String lastName, String username, String password, boolean isActive, String address, LocalDate dob, Set<Training> trainings, Set<Trainer> trainers) {
        super(id, firstName, lastName, username, password, isActive);
        this.address = address;
        this.dob = dob;
        this.trainings = trainings;
        this.trainers = trainers;
    }

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive, String address, LocalDate dob, Set<Training> trainings, Set<Trainer> trainers) {
        super(firstName, lastName, username, password, isActive);
        this.address = address;
        this.dob = dob;
        this.trainings = trainings;
        this.trainers = trainers;
    }

    @Override
    public String toString() {
        return "Trainee{" + super.toString() +
                " address='" + address + '\'' +
                ", dob=" + dob +
                '}';
    }
}
