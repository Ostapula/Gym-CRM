package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    TrainerDto createTrainerProfile(TrainerDto trainerDto);
    boolean credentialsMatchTrainer(String username, String password);
    TrainerDto updateTrainerProfile(TrainerDto trainerDto);
    Optional<TrainerDto> getTrainerByUsername(String username, String password);
    TrainerDto changePasswordTrainer(String username, String oldPassword, String newPassword);
    void activateTrainerProfile(String username, String password);
    void deactivateTrainerProfile(String username, String password);
    List<TrainingDto> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                             String traineeName);
    List<TrainerDto> getTrainersNotAssignedToTraineeByUsername(String traineeUsername, String traineePassword);
}
