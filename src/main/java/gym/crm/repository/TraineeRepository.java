package gym.crm.repository;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeRepository {
    Trainee create(Trainee trainee);
    Trainee update(Trainee trainee);
    Trainee changePassword(String username, String newPassword);
    Trainee updateTrainerList(Trainee trainee);
    boolean credentialsMatch(String username, String password);
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findAll();
    void deleteByUsername(String username);
    void setProfileActiveByUsername(String username, boolean active);
    List<Training> findTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                          String trainerName, TrainingType trainingType);
    boolean existsById(Long id);
}
