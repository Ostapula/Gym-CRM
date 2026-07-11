package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    TrainerDto createTrainerProfile(TrainerDto trainerDto);
    boolean credentialsMatchTrainer(String username, String password);
    Optional<TrainerDto> updateTrainerProfile(TrainerDto trainerDto);
    Optional<TrainerDto> getTrainerByUsername(String username);
    TrainerDto changePasswordTrainer(String username, String oldPassword, String newPassword);
    void activateTrainerProfile(String username);
    void deactivateTrainerProfile(String username);
    List<TrainingDto> getTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                             String traineeName);
    List<TrainerDto> getTrainersNotAssignedToTraineeByUsername(String traineeUsername);
}
