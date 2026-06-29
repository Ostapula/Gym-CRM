package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee createTraineeProfile(Trainee trainee);
    boolean credentialsMatchTrainee(String username, String password);
    Trainee updateTraineeProfile(Trainee trainee);
    Optional<Trainee> getTraineeByUsername(String username, String password);
    Trainee changePasswordTrainee(String username, String oldPassword, String newPassword);
    void activateTraineeProfile(String username, String password);
    void deactivateTraineeProfile(String username, String password);
    void deleteTraineeProfile(String username, String password);
    List<Training> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                          String trainerName, TrainingType trainingType);
    Trainee updateTraineesTrainerList(Trainee trainee);
    List<Trainee> getAllTrainees(String username, String password);
}
