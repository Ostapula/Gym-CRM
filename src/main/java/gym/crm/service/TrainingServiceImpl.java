package gym.crm.service;

import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
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
    private final TrainingMapper trainingMapper;

    public TrainingServiceImpl(TrainingRepository trainingRepository, TrainerRepository trainerRepository,
                               TrainingMapper trainingMapper) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainingDto createTraining(TrainingDto trainingDto, String trainerUsername, String trainerPassword) {
        requireAuthenticatedTrainer(trainerUsername, trainerPassword);
        validateCreate(trainingDto);
        log.info("Creating training name={}", trainingDto.getName());
        Training training = trainingMapper.toEntity(trainingDto);
        Training saved = trainingRepository.save(training);
        return trainingMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getAllTrainings(String trainerUsername, String trainerPassword) {
        requireAuthenticatedTrainer(trainerUsername, trainerPassword);
        log.info("Getting all trainings");
        return trainingMapper.toDtoList(trainingRepository.findAllTrainings());
    }

    private void requireAuthenticatedTrainer(String username, String password) {
        if (!trainerRepository.credentialsMatch(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainer username=" + username);
        }
    }

    private void validateCreate(TrainingDto trainingDto) {
        Objects.requireNonNull(trainingDto, "TrainingDto is required");
        if (trainingDto.getName() == null || trainingDto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        Objects.requireNonNull(trainingDto.getDate(), "date is required");
        if (trainingDto.getDuration() <= 0) {
            throw new IllegalArgumentException("duration must be greater than 0");
        }
        Objects.requireNonNull(trainingDto.getTrainerId(), "trainer is required");
        Objects.requireNonNull(trainingDto.getTraineeId(), "trainee is required");
        if (trainingDto.getTrainingTypeId() == null && trainingDto.getTrainingType() == null) {
            throw new NullPointerException("trainingType is required");
        }
    }
}
