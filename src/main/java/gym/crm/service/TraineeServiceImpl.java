package gym.crm.service;

import gym.crm.dto.TraineeMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainee;
import gym.crm.model.TrainingType;
import gym.crm.repository.TraineeRepository;
import gym.crm.util.CredentialsGenerator;
import gym.crm.dto.TraineeDto;
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
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final CredentialsGenerator credentialsGenerator;
    private final TraineeMapper traineeMapper;
    private final TrainingMapper trainingMapper;

    public TraineeServiceImpl(TraineeRepository traineeRepository, CredentialsGenerator credentialsGenerator,
                              TraineeMapper traineeMapper, TrainingMapper trainingMapper) {
        this.traineeRepository = traineeRepository;
        this.credentialsGenerator = credentialsGenerator;
        this.traineeMapper = traineeMapper;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TraineeDto createTraineeProfile(TraineeDto traineeDto) {
        validateCreate(traineeDto);
        String username = credentialsGenerator.generateUsername(traineeDto.getFirstName(), traineeDto.getLastName(),
                t -> traineeRepository.findByUsername(t).isPresent());
        String password = credentialsGenerator.generatePassword();
        traineeDto.setUsername(username);
        traineeDto.setPassword(password);
        log.info("Creating trainee profile username={}", username);
        Trainee trainee = traineeMapper.toEntity(traineeDto);
        Trainee created = traineeRepository.create(trainee);
        return traineeMapper.toDto(created);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatchTrainee(String username, String password) {
        Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            log.info("Credentials match trainee username={}", username);
            return trainee.getPassword().equals(password);
        }
        log.info("Credentials do not match trainee username={}", username);
        return false;
    }

    @Override
    public Optional<TraineeDto> updateTraineeProfile(TraineeDto traineeDto) {
        validateUpdate(traineeDto);
        if (credentialsMatchTrainee(traineeDto.getUsername(), traineeDto.getPassword())) {
            log.info("Updating trainee profile username={}", traineeDto.getUsername());
            Trainee trainee = traineeMapper.toEntity(traineeDto);
            Trainee updated = traineeRepository.update(trainee);
            return Optional.of(traineeMapper.toDto(updated));
        }
        log.info("Can't update trainee profile. Credentials do not match trainee username={}", traineeDto.getUsername());
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeDto> getTraineeByUsername(String username, String password) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username " + username + " does not exist"));

        if (!trainee.getPassword().equals(password)) {
            throw new IllegalArgumentException("Authentication failed. Wrong password.");
        }
        return Optional.of(traineeMapper.toDto(trainee));
    }

    @Override
    public TraineeDto changePasswordTrainee(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        requireAuthenticatedTrainee(username, oldPassword);
        log.info("Changing password for trainee username={}", username);
        Trainee trainee = traineeRepository.changePassword(username, newPassword);
        return traineeMapper.toDto(trainee);
    }

    @Override
    public void activateTraineeProfile(String username, String password) {
        requireAuthenticatedTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Trainee not found username=" + username));
        if (trainee.isActive()) {
            throw new IllegalStateException("Trainee profile is already active username=" + username);
        }
        log.info("Activating trainee profile username={}", username);
        traineeRepository.setProfileActiveByUsername(username, true);
    }

    @Override
    public void deactivateTraineeProfile(String username, String password) {
        requireAuthenticatedTrainee(username, password);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Trainee not found username=" + username));
        if (!trainee.isActive()) {
            throw new IllegalStateException("Trainee profile is already inactive username=" + username);
        }
        log.info("Deactivating trainee profile username={}", username);
        traineeRepository.setProfileActiveByUsername(username, false);
    }

    @Override
    public void deleteTraineeProfile(String username, String password) {
        requireAuthenticatedTrainee(username, password);
        if (traineeRepository.findByUsername(username).isPresent()) {
            log.info("Deleting trainee profile username={}", username);
            traineeRepository.deleteByUsername(username);
        } else {
            log.info("Can't delete trainee profile. Trainee not found username={}", username);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate, String trainerName, TrainingType trainingType) {
        requireAuthenticatedTrainee(username, password);
        if (traineeRepository.findByUsername(username).isPresent()) {
            log.info("Getting trainings for trainee username={}", username);
            return trainingMapper.toDtoList(traineeRepository.findTrainingsByUsername(username, fromDate, toDate, trainerName, trainingType));
        }
        log.info("Can't get trainings. Trainee not found username={}", username);
        return List.of();
    }

    @Override
    public Optional<TraineeDto> updateTraineesTrainerList(TraineeDto traineeDto) {
        validateUpdate(traineeDto);
        if (credentialsMatchTrainee(traineeDto.getUsername(), traineeDto.getPassword())) {
            log.info("Updating trainee trainer username={}", traineeDto.getUsername());
            Trainee trainee = traineeMapper.toEntity(traineeDto);
            Trainee updated = traineeRepository.updateTrainerList(trainee);
            return Optional.of(traineeMapper.toDto(updated));
        }
        log.info("Can't update trainee trainer username={}", traineeDto.getUsername());
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeDto> getAllTrainees(String username, String password) {
        requireAuthenticatedTrainee(username, password);
        return traineeMapper.toDtoList(traineeRepository.findAll());
    }

    private void requireAuthenticatedTrainee(String username, String password) {
        if (!credentialsMatchTrainee(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainee username=" + username);
        }
    }

    private void validateCreate(TraineeDto traineeDto) {
        Objects.requireNonNull(traineeDto, "TraineeDto is required");
        requireText(traineeDto.getFirstName(), "firstName");
        requireText(traineeDto.getLastName(), "lastName");
        Objects.requireNonNull(traineeDto.getDob(), "dob is required");
    }

    private void validateUpdate(TraineeDto traineeDto) {
        validateCreate(traineeDto);
        Objects.requireNonNull(traineeDto.getId(), "id is required for update");
        requireText(traineeDto.getUsername(), "username");
        requireText(traineeDto.getPassword(), "password");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
