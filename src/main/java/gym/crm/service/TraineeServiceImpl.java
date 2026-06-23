package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.util.CredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import gym.crm.dao.TraineeDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private TraineeDao traineeDao;
    private CredentialsGenerator credentialsGenerator;

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setCredentialsGenerator(CredentialsGenerator credentialsGenerator) {
        this.credentialsGenerator = credentialsGenerator;
    }

    @Override
    public Trainee createTraineeProfile(String firstName, String lastName, boolean isActive, String address, LocalDate dateOfBirth) {
        String username = credentialsGenerator.generateUsername(firstName, lastName, candidate -> traineeDao.selectByUsername(candidate).isPresent());
        String password = credentialsGenerator.generatePassword();
        var trainee = new Trainee(firstName, lastName, username, password, isActive, nextId(), address, dateOfBirth);
        log.info("Creating trainee profile id={} username={}", trainee.getUserId(), username);
        return traineeDao.create(trainee);
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        log.info("Updating trainee profile id={} username={}", trainee.getUserId(), trainee.getUsername());
        if (traineeDao.selectById(trainee.getUserId()).isEmpty()) {
            log.warn("Cannot update trainee profile - id={} not found", trainee.getUserId());
            throw new IllegalArgumentException("Trainee not found: " + trainee.getUserId());
        }
        return traineeDao.update(trainee);
    }

    @Override
    public void deleteTrainee(Long id) {
        log.info("Deleting trainee profile id={}", id);
        traineeDao.delete(id);
    }

    @Override
    public Optional<Trainee> getTrainee(Long id) {
        log.debug("Retrieving trainee profile id={}", id);
        return traineeDao.selectById(id);
    }

    @Override
    public List<Trainee> getAllTrainees() {
        log.debug("Retrieving all trainee profiles");
        return traineeDao.selectAll();
    }

    private Long nextId() {
        return traineeDao.selectAll().stream()
                .mapToLong(Trainee::getUserId)
                .filter(Objects::nonNull)
                .max()
                .orElse(0L) + 1;
    }
}
