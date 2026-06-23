package gym.crm.facade;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.service.TraineeService;
import gym.crm.service.TrainerService;
import gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        log.info("GymFacade initialized with all domain services");
    }

    public Trainee createTrainee(String firstName, String lastName, boolean isActive,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.createTraineeProfile(firstName, lastName, isActive, address, dateOfBirth);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.updateTrainee(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.getTrainee(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAllTrainees();
    }

    public Trainer createTrainer(String firstName, String lastName, boolean isActive,
                                 TrainingType specialization) {
        return trainerService.createTrainerProfile(firstName, lastName, isActive, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.updateTrainer(trainer);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.getTrainer(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, int trainingDuration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingType,
                trainingDate, trainingDuration);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.getTraining(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }
}
