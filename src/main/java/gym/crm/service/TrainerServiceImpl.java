package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainer;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.util.CredentialsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final CredentialsGenerator credentialsGenerator;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public TrainerServiceImpl(TrainerRepository trainerRepository, TraineeRepository traineeRepository,
                              CredentialsGenerator credentialsGenerator, TrainerMapper trainerMapper,
                              TrainingMapper trainingMapper) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.credentialsGenerator = credentialsGenerator;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainerDto createTrainerProfile(TrainerDto trainerDto) {
        validateCreate(trainerDto);
        String username = credentialsGenerator.generateUsername(trainerDto.getFirstName(), trainerDto.getLastName(),
                t -> trainerRepository.findByUsername(t).isPresent());
        String password = credentialsGenerator.generatePassword();
        trainerDto.setUsername(username);
        trainerDto.setPassword(password);
        log.info("Creating trainer profile username={}", username);
        Trainer trainer = trainerMapper.toEntity(trainerDto);
        Trainer created = trainerRepository.create(trainer);
        return trainerMapper.toDto(created);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatchTrainer(String username, String password) {
        Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            log.info("Credentials match trainer username={}", username);
            return trainer.getPassword().equals(password);
        }
        log.info("Credentials do not match trainer username={}", username);
        return false;
    }

    @Override
    public Optional<TrainerDto> updateTrainerProfile(TrainerDto trainerDto) {
        validateUpdate(trainerDto);
        if (credentialsMatchTrainer(trainerDto.getUsername(), trainerDto.getPassword())) {
            log.info("Updating trainer profile username={}", trainerDto.getUsername());
            Trainer trainer = trainerMapper.toEntity(trainerDto);
            Trainer updated = trainerRepository.update(trainer);
            return Optional.of(trainerMapper.toDto(updated));
        }
        log.info("Can't update trainer profile. Credentials do not match trainer username={}", trainerDto.getUsername());
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerDto> getTrainerByUsername(String username, String password) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username " + username + " does not exist"));

        if (!trainer.getPassword().equals(password)) {
            throw new IllegalArgumentException("Authentication failed. Wrong password.");
        }
        return Optional.of(trainerMapper.toDto(trainer));
    }

    @Override
    public TrainerDto changePasswordTrainer(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        requireAuthenticatedTrainer(username, oldPassword);
        log.info("Changing password for trainer username={}", username);
        Trainer trainer = trainerRepository.changePassword(username, newPassword);
        return trainerMapper.toDto(trainer);
    }

    @Override
    public void activateTrainerProfile(String username, String password) {
        requireAuthenticatedTrainer(username, password);
        Trainer trainer = trainerRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Trainer not found username=" + username));
        if (trainer.isActive()) {
            throw new IllegalStateException("Trainer profile is already active username=" + username);
        }
        log.info("Activating trainer profile username={}", username);
        trainerRepository.setProfileActiveByUsername(username, true);
    }

    @Override
    public void deactivateTrainerProfile(String username, String password) {
        requireAuthenticatedTrainer(username, password);
        Trainer trainer = trainerRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Trainer not found username=" + username));
        if (!trainer.isActive()) {
            throw new IllegalStateException("Trainer profile is already inactive username=" + username);
        }
        log.info("Deactivating trainer profile username={}", username);
        trainerRepository.setProfileActiveByUsername(username, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                                    String traineeName) {
        requireAuthenticatedTrainer(username, password);
        if (trainerRepository.findByUsername(username).isPresent()) {
            log.info("Getting trainings for trainer username={}", username);
            return trainingMapper.toDtoList(trainerRepository.findTrainingsByUsername(username, fromDate, toDate, traineeName));
        }
        log.info("Can't get trainings. Trainer not found username={}", username);
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerDto> getTrainersNotAssignedToTraineeByUsername(String traineeUsername, String traineePassword) {
        if (!traineeRepository.credentialsMatch(traineeUsername, traineePassword)) {
            throw new IllegalArgumentException("Authentication failed for trainee username=" + traineeUsername);
        }
        log.info("Getting trainers not assigned to trainee username={}", traineeUsername);
        return trainerMapper.toDtoList(trainerRepository.findTrainersNotAssignedToTraineeByUsername(traineeUsername));
    }

    private void requireAuthenticatedTrainer(String username, String password) {
        if (!credentialsMatchTrainer(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainer username=" + username);
        }
    }

    private void validateCreate(TrainerDto trainerDto) {
        Objects.requireNonNull(trainerDto, "TrainerDto is required");
        requireText(trainerDto.getFirstName(), "firstName");
        requireText(trainerDto.getLastName(), "lastName");
        if (trainerDto.getSpecializationId() == null && trainerDto.getSpecializationType() == null) {
            throw new NullPointerException("specialization is required");
        }
    }

    private void validateUpdate(TrainerDto trainerDto) {
        validateCreate(trainerDto);
        Objects.requireNonNull(trainerDto.getId(), "id is required for update");
        requireText(trainerDto.getUsername(), "username");
        requireText(trainerDto.getPassword(), "password");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
