package gym.crm.service;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingMapper trainingMapper;

    public TrainingServiceImpl(TrainingRepository trainingRepository, TrainerRepository trainerRepository,
                               TraineeRepository traineeRepository, TrainingMapper trainingMapper) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public void createTraining(AddTrainingRequest request) {
        validate(request);
        Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer username=" + request.getTrainerUsername() + " not found"));
        Trainee trainee = traineeRepository.findByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee username=" + request.getTraineeUsername() + " not found"));

        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(trainer.getSpecialization());
        training.setName(request.getTrainingName());
        training.setDate(request.getTrainingDate());
        training.setDuration(request.getTrainingDuration());

        log.info("Creating training name={} trainer={} trainee={}", request.getTrainingName(),
                request.getTrainerUsername(), request.getTraineeUsername());
        trainingRepository.save(training);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getAllTrainings() {
        log.info("Getting all trainings");
        return trainingMapper.toDtoList(trainingRepository.findAllTrainings());
    }

    private void validate(AddTrainingRequest request) {
        Objects.requireNonNull(request, "request is required");
        requireText(request.getTraineeUsername(), "traineeUsername");
        requireText(request.getTrainerUsername(), "trainerUsername");
        requireText(request.getTrainingName(), "trainingName");
        Objects.requireNonNull(request.getTrainingDate(), "trainingDate is required");
        if (request.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("trainingDuration must be greater than 0");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
