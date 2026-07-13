package gym.crm.facade;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerSummaryDto;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.model.TrainingType;
import gym.crm.service.TraineeService;
import gym.crm.service.TrainerService;
import gym.crm.service.TrainingService;
import gym.crm.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService,
                     TrainingService trainingService, TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    public TraineeDto createTrainee(TraineeDto traineeDto) {
        return traineeService.createTraineeProfile(traineeDto);
    }

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.credentialsMatchTrainee(username, password);
    }

    public Optional<TraineeDto> getTrainee(String username) {
        return traineeService.getTraineeByUsername(username);
    }

    public Optional<TraineeDto> updateTrainee(TraineeDto traineeDto) {
        return traineeService.updateTraineeProfile(traineeDto);
    }

    public TraineeDto changeTraineePassword(String username, String oldPassword, String newPassword) {
        return traineeService.changePasswordTrainee(username, oldPassword, newPassword);
    }

    public void activateTrainee(String username) {
        traineeService.activateTraineeProfile(username);
    }

    public void deactivateTrainee(String username) {
        traineeService.deactivateTraineeProfile(username);
    }

    public void deleteTrainee(String username) {
        traineeService.deleteTraineeProfile(username);
    }

    public List<TrainerSummaryDto> updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        return traineeService.updateTraineesTrainerList(traineeUsername, trainerUsernames);
    }

    public List<TraineeDto> getAllTrainees() {
        return traineeService.getAllTrainees();
    }

    public List<TrainingDto> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                                 String trainerName, TrainingType trainingType) {
        return traineeService.getTrainingsByUsername(username, fromDate, toDate, trainerName, trainingType);
    }

    public TrainerDto createTrainer(TrainerDto trainerDto) {
        return trainerService.createTrainerProfile(trainerDto);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.credentialsMatchTrainer(username, password);
    }

    public Optional<TrainerDto> getTrainer(String username) {
        return trainerService.getTrainerByUsername(username);
    }

    public Optional<TrainerDto> updateTrainer(TrainerDto trainerDto) {
        return trainerService.updateTrainerProfile(trainerDto);
    }

    public TrainerDto changeTrainerPassword(String username, String oldPassword, String newPassword) {
        return trainerService.changePasswordTrainer(username, oldPassword, newPassword);
    }

    public void activateTrainer(String username) {
        trainerService.activateTrainerProfile(username);
    }

    public void deactivateTrainer(String username) {
        trainerService.deactivateTrainerProfile(username);
    }

    public List<TrainerDto> getTrainersNotAssignedToTrainee(String traineeUsername) {
        return trainerService.getTrainersNotAssignedToTraineeByUsername(traineeUsername);
    }

    public List<TrainingDto> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                                 String traineeName) {
        return trainerService.getTrainingsByUsername(username, fromDate, toDate, traineeName);
    }

    public void addTraining(AddTrainingRequest request) {
        trainingService.createTraining(request);
    }

    public List<TrainingDto> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    public TrainingTypeEntityDto getTrainingType(TrainingType type) {
        return trainingTypeService.getByType(type);
    }

    public List<TrainingTypeEntityDto> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
