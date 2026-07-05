package gym.crm.facade;

import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerDto;
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

    public Optional<TraineeDto> getTrainee(String username, String password) {
        return traineeService.getTraineeByUsername(username, password);
    }

    public Optional<TraineeDto> updateTrainee(TraineeDto traineeDto) {
        return traineeService.updateTraineeProfile(traineeDto);
    }

    public TraineeDto changeTraineePassword(String username, String oldPassword, String newPassword) {
        return traineeService.changePasswordTrainee(username, oldPassword, newPassword);
    }

    public void activateTrainee(String username, String password) {
        traineeService.activateTraineeProfile(username, password);
    }

    public void deactivateTrainee(String username, String password) {
        traineeService.deactivateTraineeProfile(username, password);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteTraineeProfile(username, password);
    }

    public Optional<TraineeDto> updateTraineeTrainers(TraineeDto traineeDto) {
        return traineeService.updateTraineesTrainerList(traineeDto);
    }

    public List<TraineeDto> getAllTrainees(String username, String password) {
        return traineeService.getAllTrainees(username, password);
    }

    public List<TrainingDto> getTraineeTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                                 String trainerName, TrainingType trainingType) {
        return traineeService.getTrainingsByUsername(username, password, fromDate, toDate, trainerName, trainingType);
    }

    public TrainerDto createTrainer(TrainerDto trainerDto) {
        return trainerService.createTrainerProfile(trainerDto);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.credentialsMatchTrainer(username, password);
    }

    public Optional<TrainerDto> getTrainer(String username, String password) {
        return trainerService.getTrainerByUsername(username, password);
    }

    public Optional<TrainerDto> updateTrainer(TrainerDto trainerDto) {
        return trainerService.updateTrainerProfile(trainerDto);
    }

    public TrainerDto changeTrainerPassword(String username, String oldPassword, String newPassword) {
        return trainerService.changePasswordTrainer(username, oldPassword, newPassword);
    }

    public void activateTrainer(String username, String password) {
        trainerService.activateTrainerProfile(username, password);
    }

    public void deactivateTrainer(String username, String password) {
        trainerService.deactivateTrainerProfile(username, password);
    }

    public List<TrainerDto> getTrainersNotAssignedToTrainee(String traineeUsername, String traineePassword) {
        return trainerService.getTrainersNotAssignedToTraineeByUsername(traineeUsername, traineePassword);
    }

    public List<TrainingDto> getTrainerTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                                 String traineeName) {
        return trainerService.getTrainingsByUsername(username, password, fromDate, toDate, traineeName);
    }

    public TrainingDto addTraining(TrainingDto trainingDto, String trainerUsername, String trainerPassword) {
        return trainingService.createTraining(trainingDto, trainerUsername, trainerPassword);
    }

    public List<TrainingDto> getAllTrainings(String trainerUsername, String trainerPassword) {
        return trainingService.getAllTrainings(trainerUsername, trainerPassword);
    }

    public TrainingTypeEntityDto getTrainingType(TrainingType type) {
        return trainingTypeService.getByType(type);
    }

    public List<TrainingTypeEntityDto> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
