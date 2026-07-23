package gym.crm.service;

import gym.crm.dto.*;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.ValidationException;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.metrics.GymMetricsRecorder;
import gym.crm.model.TrainingType;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.util.CredentialsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final CredentialsGenerator credentialsGenerator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final GymMetricsRecorder metricsRecorder;
    private final PasswordEncoder passwordEncoder;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                              CredentialsGenerator credentialsGenerator, TraineeMapper traineeMapper,
                              TrainerMapper trainerMapper, TrainingMapper trainingMapper,
                              GymMetricsRecorder metricsRecorder, PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.credentialsGenerator = credentialsGenerator;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
        this.metricsRecorder = metricsRecorder;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TraineeDto createTraineeProfile(TraineeDto traineeDto) {
        validateCreate(traineeDto);
        String username = credentialsGenerator.generateUsername(traineeDto.getFirstName(), traineeDto.getLastName(),
                t -> traineeRepository.findByUsername(t).isPresent());
        String password = credentialsGenerator.generatePassword();
        traineeDto.setActive(true);
        traineeDto.setUsername(username);
        String encodedPassword = passwordEncoder.encode(password);
        traineeDto.setPassword(encodedPassword);
        log.info("Creating trainee profile username={}", username);
        Trainee trainee = traineeMapper.toEntity(traineeDto);
        Trainee created = traineeRepository.create(trainee);
        metricsRecorder.recordTraineeRegistered();
        TraineeDto result = traineeMapper.toDto(created);
        result.setPassword(password);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatchTrainee(String username, String password) {
        Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);
        if (traineeOpt.isPresent()) {
            return passwordEncoder.matches(password, traineeOpt.get().getPassword());
        }
        log.info("Credentials do not match trainee username={}", username);
        return false;
    }

    @Override
    public Optional<TraineeDto> updateTraineeProfile(TraineeDto traineeDto) {
        validateUpdate(traineeDto);
        Trainee trainee = requireTrainee(traineeDto.getUsername());
        log.info("Updating trainee profile username={}", traineeDto.getUsername());
        trainee.setFirstName(traineeDto.getFirstName());
        trainee.setLastName(traineeDto.getLastName());
        trainee.setActive(traineeDto.isActive());
        trainee.setAddress(traineeDto.getAddress());
        trainee.setDob(traineeDto.getDob());
        return Optional.of(traineeMapper.toDto(trainee));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeDto> getTraineeByUsername(String username) {
        Trainee trainee = requireTrainee(username);
        return Optional.of(traineeMapper.toDto(trainee));
    }

    @Override
    public TraineeDto changePasswordTrainee(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        if (!credentialsMatchTrainee(username, oldPassword)) {
            throw new AuthenticationFailedException("Authentication failed for trainee username=" + username);
        }
        log.info("Changing password for trainee username={}", username);
        Trainee trainee = traineeRepository.changePassword(username, passwordEncoder.encode(newPassword));
        return traineeMapper.toDto(trainee);
    }

    @Override
    public void activateTraineeProfile(String username) {
        Trainee trainee = requireTrainee(username);
        if (trainee.isActive()) {
            throw new ProfileStatusException("Trainee profile is already active username=" + username);
        }
        log.info("Activating trainee profile username={}", username);
        traineeRepository.setProfileActiveByUsername(username, true);
    }

    @Override
    public void deactivateTraineeProfile(String username) {
        Trainee trainee = requireTrainee(username);
        if (!trainee.isActive()) {
            throw new ProfileStatusException("Trainee profile is already inactive username=" + username);
        }
        log.info("Deactivating trainee profile username={}", username);
        traineeRepository.setProfileActiveByUsername(username, false);
    }

    @Override
    public void deleteTraineeProfile(String username) {
        requireTrainee(username);
        log.info("Deleting trainee profile username={}", username);
        traineeRepository.deleteByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                                    String trainerName, TrainingType trainingType) {
        requireTrainee(username);
        log.info("Getting trainings for trainee username={}", username);
        return trainingMapper.toDtoList(traineeRepository.findTrainingsByUsername(username, fromDate, toDate, trainerName, trainingType));
    }

    @Override
    public List<TrainerSummaryDto> updateTraineesTrainerList(String traineeUsername, List<String> trainerUsernames) {
        requireText(traineeUsername, "username");
        Trainee trainee = requireTrainee(traineeUsername);
        log.info("Updating trainee trainer list username={}", traineeUsername);
        Set<Trainer> trainers = resolveTrainers(trainerUsernames);
        trainee.setTrainers(trainers);
        return trainerMapper.toSummaryList(trainers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeDto> getAllTrainees() {
        return traineeMapper.toDtoList(traineeRepository.findAll());
    }

    private Trainee requireTrainee(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee username=" + username + " not found"));
    }

    private Set<Trainer> resolveTrainers(List<String> trainerUsernames) {
        if (trainerUsernames == null) {
            return Set.of();
        }
        return trainerUsernames.stream()
                .map(username -> trainerRepository.findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer username=" + username + " not found")))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateCreate(TraineeDto traineeDto) {
        Objects.requireNonNull(traineeDto, "TraineeDto is required");
        requireText(traineeDto.getFirstName(), "firstName");
        requireText(traineeDto.getLastName(), "lastName");
    }

    private void validateUpdate(TraineeDto traineeDto) {
        validateCreate(traineeDto);
        requireText(traineeDto.getUsername(), "username");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
