package gym.crm.service;

import gym.crm.model.Trainer;
import gym.crm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer createTrainerProfile(Trainer trainer);
    boolean credentialsMatchTrainer(String username, String password);
    Trainer updateTrainerProfile(Trainer trainer);
    Optional<Trainer> getTrainerByUsername(String username, String password);
    Trainer changePasswordTrainer(String username, String oldPassword, String newPassword);
    void activateTrainerProfile(String username, String password);
    void deactivateTrainerProfile(String username, String password);
    List<Training> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                          String traineeName);
    List<Trainer> getTrainersNotAssignedToTraineeByUsername(String traineeUsername, String traineePassword);
}

