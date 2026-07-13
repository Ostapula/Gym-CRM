package gym.crm.service;

import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerSummaryDto;
import gym.crm.dto.TrainingDto;
import gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    TraineeDto createTraineeProfile(TraineeDto traineeDto);
    boolean credentialsMatchTrainee(String username, String password);
    Optional<TraineeDto> updateTraineeProfile(TraineeDto traineeDto);
    Optional<TraineeDto> getTraineeByUsername(String username);
    TraineeDto changePasswordTrainee(String username, String oldPassword, String newPassword);
    void activateTraineeProfile(String username);
    void deactivateTraineeProfile(String username);
    void deleteTraineeProfile(String username);
    List<TrainingDto> getTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                             String trainerName, TrainingType trainingType);
    List<TrainerSummaryDto> updateTraineesTrainerList(String traineeUsername, List<String> trainerUsernames);
    List<TraineeDto> getAllTrainees();
}
