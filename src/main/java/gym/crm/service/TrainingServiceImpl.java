package gym.crm.service;

import gym.crm.model.Training;
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

    public TrainingServiceImpl(TrainingRepository trainingRepository, TrainerRepository trainerRepository) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public Training createTraining(Training training, String trainerUsername, String trainerPassword) {
        requireAuthenticatedTrainer(trainerUsername, trainerPassword);
        validateCreate(training);
        log.info("Creating training name={}", training.getName());
        return trainingRepository.save(training);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings(String trainerUsername, String trainerPassword) {
        requireAuthenticatedTrainer(trainerUsername, trainerPassword);
        log.info("Getting all trainings");
        return trainingRepository.findAllTrainings();
    }

    private void requireAuthenticatedTrainer(String username, String password) {
        if (!trainerRepository.credentialsMatch(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainer username=" + username);
        }
    }

    private void validateCreate(Training training) {
        Objects.requireNonNull(training, "Training is required");
        if (training.getName() == null || training.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        Objects.requireNonNull(training.getDate(), "date is required");
        if (training.getDuration() <= 0) {
            throw new IllegalArgumentException("duration must be greater than 0");
        }
        Objects.requireNonNull(training.getTrainer(), "trainer is required");
        Objects.requireNonNull(training.getTrainee(), "trainee is required");
        Objects.requireNonNull(training.getTrainingType(), "trainingType is required");
    }
}

