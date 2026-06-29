package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
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

    public TrainerServiceImpl(TrainerRepository trainerRepository, TraineeRepository traineeRepository,
                              CredentialsGenerator credentialsGenerator) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Override
    public Trainer createTrainerProfile(Trainer trainer) {
        validateCreate(trainer);
        String username = credentialsGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName(),
                t -> trainerRepository.findByUsername(t).isPresent());
        String password = credentialsGenerator.generatePassword();
        trainer.setUsername(username);
        trainer.setPassword(password);
        log.info("Creating trainer profile username={}", username);
        return trainerRepository.create(trainer);
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
    public Trainer updateTrainerProfile(Trainer trainer) {
        validateUpdate(trainer);
        if (credentialsMatchTrainer(trainer.getUsername(), trainer.getPassword())) {
            log.info("Updating trainer profile username={}", trainer.getUsername());
            return trainerRepository.update(trainer);
        }
        log.info("Can't update trainer profile. Credentials do not match trainer username={}", trainer.getUsername());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerByUsername(String username, String password) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username " + username + " does not exist"));

        if (!trainer.getPassword().equals(password)) {
            throw new IllegalArgumentException("Authentication failed. Wrong password.");
        }
        return Optional.of(trainer);
    }

    @Override
    public Trainer changePasswordTrainer(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        requireAuthenticatedTrainer(username, oldPassword);
        log.info("Changing password for trainer username={}", username);
        return trainerRepository.changePassword(username, newPassword);
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
    public List<Training> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate,
                                                 String traineeName) {
        requireAuthenticatedTrainer(username, password);
        if (trainerRepository.findByUsername(username).isPresent()) {
            log.info("Getting trainings for trainer username={}", username);
            return trainerRepository.findTrainingsByUsername(username, fromDate, toDate, traineeName);
        }
        log.info("Can't get trainings. Trainer not found username={}", username);
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTraineeByUsername(String traineeUsername, String traineePassword) {
        if (!traineeRepository.credentialsMatch(traineeUsername, traineePassword)) {
            throw new IllegalArgumentException("Authentication failed for trainee username=" + traineeUsername);
        }
        log.info("Getting trainers not assigned to trainee username={}", traineeUsername);
        return trainerRepository.findTrainersNotAssignedToTraineeByUsername(traineeUsername);
    }

    private void requireAuthenticatedTrainer(String username, String password) {
        if (!credentialsMatchTrainer(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainer username=" + username);
        }
    }

    private void validateCreate(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");
        requireText(trainer.getFirstName(), "firstName");
        requireText(trainer.getLastName(), "lastName");
        Objects.requireNonNull(trainer.getSpecialization(), "specialization is required");
    }

    private void validateUpdate(Trainer trainer) {
        validateCreate(trainer);
        Objects.requireNonNull(trainer.getId(), "id is required for update");
        requireText(trainer.getUsername(), "username");
        requireText(trainer.getPassword(), "password");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

