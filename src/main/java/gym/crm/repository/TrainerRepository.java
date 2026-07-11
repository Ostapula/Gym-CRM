package gym.crm.repository;

import gym.crm.model.Trainer;
import gym.crm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository {
    Trainer create(Trainer trainer);
    Trainer update(Trainer trainer);
    Trainer changePassword(String username, String newPassword);
    boolean credentialsMatch(String username, String password);
    Optional<Trainer> findByUsername(String username);
    void setProfileActiveByUsername(String username, boolean active);
    boolean existsById(Long id);
    List<Training> findTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                           String traineeName);
    List<Trainer> findTrainersNotAssignedToTraineeByUsername(String traineeUsername);
}
