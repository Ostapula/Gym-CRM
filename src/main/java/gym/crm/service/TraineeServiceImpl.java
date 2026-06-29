package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.repository.TraineeRepository;
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
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final CredentialsGenerator credentialsGenerator;

    public TraineeServiceImpl(TraineeRepository traineeRepository, CredentialsGenerator credentialsGenerator) {
        this.traineeRepository = traineeRepository;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Override
    public Trainee createTraineeProfile(Trainee trainee) {
        validateCreate(trainee);
        String username = credentialsGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName(),
                t -> traineeRepository.findByUsername(t).isPresent());
        String password = credentialsGenerator.generatePassword();
        trainee.setUsername(username);
        trainee.setPassword(password);
        log.info("Creating trainee profile username={}", username);
        return traineeRepository.create(trainee);
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
    public Trainee updateTraineeProfile(Trainee trainee) {
        validateUpdate(trainee);
        if (credentialsMatchTrainee(trainee.getUsername(), trainee.getPassword())) {
            log.info("Updating trainee profile username={}", trainee.getUsername());
            return traineeRepository.update(trainee);
        }
        log.info("Can't update trainee profile. Credentials do not match trainee username={}", trainee.getUsername());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeByUsername(String username, String password) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username " + username + " does not exist"));

        if (!trainee.getPassword().equals(password)) {
            throw new IllegalArgumentException("Authentication failed. Wrong password.");
        }
        return Optional.of(trainee);
    }

    @Override
    public Trainee changePasswordTrainee(String username, String oldPassword, String newPassword) {
        requireText(username, "username");
        requireText(oldPassword, "oldPassword");
        requireText(newPassword, "newPassword");
        requireAuthenticatedTrainee(username, oldPassword);
        log.info("Changing password for trainee username={}", username);
        return traineeRepository.changePassword(username, newPassword);
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
    public List<Training> getTrainingsByUsername(String username, String password, LocalDate fromDate, LocalDate toDate, String trainerName, TrainingType trainingType) {
        requireAuthenticatedTrainee(username, password);
        if (traineeRepository.findByUsername(username).isPresent()) {
            log.info("Getting trainings for trainee username={}", username);
            return traineeRepository.findTrainingsByUsername(username, fromDate, toDate, trainerName, trainingType);
        }
        log.info("Can't get trainings. Trainee not found username={}", username);
        return List.of();
    }

    @Override
    public Trainee updateTraineesTrainerList(Trainee trainee) {
        validateUpdate(trainee);
        if (credentialsMatchTrainee(trainee.getUsername(), trainee.getPassword())) {
            log.info("Updating trainee trainer username={}", trainee.getUsername());
            return traineeRepository.updateTrainerList(trainee);
        }
        log.info("Can't update trainee trainer username={}", trainee.getUsername());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees(String username, String password) {
        requireAuthenticatedTrainee(username, password);
        return traineeRepository.findAll();
    }

    private void requireAuthenticatedTrainee(String username, String password) {
        if (!credentialsMatchTrainee(username, password)) {
            throw new IllegalArgumentException("Authentication failed for trainee username=" + username);
        }
    }

    private void validateCreate(Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee is required");
        requireText(trainee.getFirstName(), "firstName");
        requireText(trainee.getLastName(), "lastName");
        Objects.requireNonNull(trainee.getDob(), "dob is required");
    }

    private void validateUpdate(Trainee trainee) {
        validateCreate(trainee);
        Objects.requireNonNull(trainee.getId(), "id is required for update");
        requireText(trainee.getUsername(), "username");
        requireText(trainee.getPassword(), "password");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
