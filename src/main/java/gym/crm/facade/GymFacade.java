package gym.crm.facade;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
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

    public Trainee createTrainee(Trainee trainee) {
        return traineeService.createTraineeProfile(trainee);
    }

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.credentialsMatchTrainee(username, password);
    }

    public Optional<Trainee> getTrainee(String username, String password) {
        return traineeService.getTraineeByUsername(username, password);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.updateTraineeProfile(trainee);
    }

    public Trainee changeTraineePassword(String username, String oldPassword, String newPassword) {
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

    public Trainee updateTraineeTrainers(Trainee trainee) {
        return traineeService.updateTraineesTrainerList(trainee);
    }

    public List<Trainee> getAllTrainees(String username, String password) {
        return traineeService.getAllTrainees(username, password);
    }

    public List<Training> getTraineeTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                              String trainerName, TrainingType trainingType) {
        return traineeService.getTrainingsByUsername(username, password, fromDate, toDate, trainerName, trainingType);
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerService.createTrainerProfile(trainer);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.credentialsMatchTrainer(username, password);
    }

    public Optional<Trainer> getTrainer(String username, String password) {
        return trainerService.getTrainerByUsername(username, password);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.updateTrainerProfile(trainer);
    }

    public Trainer changeTrainerPassword(String username, String oldPassword, String newPassword) {
        return trainerService.changePasswordTrainer(username, oldPassword, newPassword);
    }

    public void activateTrainer(String username, String password) {
        trainerService.activateTrainerProfile(username, password);
    }

    public void deactivateTrainer(String username, String password) {
        trainerService.deactivateTrainerProfile(username, password);
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername, String traineePassword) {
        return trainerService.getTrainersNotAssignedToTraineeByUsername(traineeUsername, traineePassword);
    }

    public List<Training> getTrainerTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        return trainerService.getTrainingsByUsername(username, password, fromDate, toDate, traineeName);
    }

    public Training addTraining(Training training, String trainerUsername, String trainerPassword) {
        return trainingService.createTraining(training, trainerUsername, trainerPassword);
    }

    public List<Training> getAllTrainings(String trainerUsername, String trainerPassword) {
        return trainingService.getAllTrainings(trainerUsername, trainerPassword);
    }

    public TrainingTypeEntity getTrainingType(TrainingType type) {
        return trainingTypeService.getByType(type);
    }

    public List<TrainingTypeEntity> getAllTrainingTypes() {
        return trainingTypeService.getAll();
    }
}
