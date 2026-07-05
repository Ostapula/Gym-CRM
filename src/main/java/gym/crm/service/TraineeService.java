package gym.crm.service;

import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainingDto;
import gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    TraineeDto createTraineeProfile(TraineeDto traineeDto);
    boolean credentialsMatchTrainee(String username, String password);
    TraineeDto updateTraineeProfile(TraineeDto traineeDto);
    Optional<TraineeDto> getTraineeByUsername(String username, String password);
    TraineeDto changePasswordTrainee(String username, String oldPassword, String newPassword);
    void activateTraineeProfile(String username, String password);
    void deactivateTraineeProfile(String username, String password);
    void deleteTraineeProfile(String username, String password);
    List<TrainingDto> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                             String trainerName, TrainingType trainingType);
    TraineeDto updateTraineesTrainerList(TraineeDto traineeDto);
    List<TraineeDto> getAllTrainees(String username, String password);
}
