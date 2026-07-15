package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.ValidationException;
import gym.crm.model.Trainer;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainerRepository;
import gym.crm.repository.TrainingTypeRepository;
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
    private final TrainingTypeRepository trainingTypeRepository;
    private final CredentialsGenerator credentialsGenerator;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public TrainerServiceImpl(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository,
                              CredentialsGenerator credentialsGenerator, TrainerMapper trainerMapper,
                              TrainingMapper trainingMapper) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
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
        trainerDto.setActive(true);
        trainerDto.setUsername(username);
        trainerDto.setPassword(password);
        log.info("Creating trainer profile username={}", username);
        Trainer trainer = trainerMapper.toEntity(trainerDto);
        trainer.setSpecialization(resolveSpecialization(trainerDto));
        Trainer created = trainerRepository.create(trainer);
        return trainerMapper.toDto(created);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatchTrainer(String username, String password) {
        Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);
        if (trainerOpt.isPresent()) {
            return trainerOpt.get().getPassword().equals(password);
        }
        log.info("Credentials do not match trainer username={}", username);
        return false;
    }

    @Override
    public Optional<TrainerDto> updateTrainerProfile(TrainerDto trainerDto) {
        validateUpdate(trainerDto);
        Trainer trainer = requireTrainer(trainerDto.getUsername());
        log.info("Updating trainer profile username={}", trainerDto.getUsername());
        trainer.setFirstName(trainerDto.getFirstName());
        trainer.setLastName(trainerDto.getLastName());
        trainer.setActive(trainerDto.isActive());
        return Optional.of(trainerMapper.toDto(trainer));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerDto> getTrainerByUsername(String username) {
        Trainer trainer = requireTrainer(username);
        return Optional.of(trainerMapper.toDto(trainer));
    }

    @Override
    public TrainerDto changePasswordTrainer(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        if (!credentialsMatchTrainer(username, oldPassword)) {
            throw new AuthenticationFailedException("Authentication failed for trainer username=" + username);
        }
        log.info("Changing password for trainer username={}", username);
        Trainer trainer = trainerRepository.changePassword(username, newPassword);
        return trainerMapper.toDto(trainer);
    }

    @Override
    public void activateTrainerProfile(String username) {
        Trainer trainer = requireTrainer(username);
        if (trainer.isActive()) {
            throw new ProfileStatusException("Trainer profile is already active username=" + username);
        }
        log.info("Activating trainer profile username={}", username);
        trainerRepository.setProfileActiveByUsername(username, true);
    }

    @Override
    public void deactivateTrainerProfile(String username) {
        Trainer trainer = requireTrainer(username);
        if (!trainer.isActive()) {
            throw new ProfileStatusException("Trainer profile is already inactive username=" + username);
        }
        log.info("Deactivating trainer profile username={}", username);
        trainerRepository.setProfileActiveByUsername(username, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                                    String traineeName) {
        requireTrainer(username);
        log.info("Getting trainings for trainer username={}", username);
        return trainingMapper.toDtoList(trainerRepository.findTrainingsByUsername(username, fromDate, toDate, traineeName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerDto> getTrainersNotAssignedToTraineeByUsername(String traineeUsername) {
        log.info("Getting trainers not assigned to trainee username={}", traineeUsername);
        return trainerMapper.toDtoList(trainerRepository.findTrainersNotAssignedToTraineeByUsername(traineeUsername));
    }

    private TrainingTypeEntity resolveSpecialization(TrainerDto trainerDto) {
        if (trainerDto.getSpecializationType() != null) {
            return trainingTypeRepository.findByType(trainerDto.getSpecializationType())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Unknown training type " + trainerDto.getSpecializationType()));
        }
        return trainingTypeRepository.findAll().stream()
                .filter(type -> type.getId().equals(trainerDto.getSpecializationId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unknown training type id " + trainerDto.getSpecializationId()));
    }

    private Trainer requireTrainer(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer username=" + username + " not found"));
    }

    private void validateCreate(TrainerDto trainerDto) {
        Objects.requireNonNull(trainerDto, "TrainerDto is required");
        requireText(trainerDto.getFirstName(), "firstName");
        requireText(trainerDto.getLastName(), "lastName");
        if (trainerDto.getSpecializationId() == null && trainerDto.getSpecializationType() == null) {
            throw new ValidationException("specialization is required");
        }
    }

    private void validateUpdate(TrainerDto trainerDto) {
        Objects.requireNonNull(trainerDto, "TrainerDto is required");
        requireText(trainerDto.getUsername(), "username");
        requireText(trainerDto.getFirstName(), "firstName");
        requireText(trainerDto.getLastName(), "lastName");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
