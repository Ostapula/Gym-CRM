package gym.crm.service;

import gym.crm.dao.TrainerDao;
import gym.crm.model.Trainer;
import gym.crm.model.TrainingType;
import gym.crm.util.CredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private TrainerDao trainerDao;
    private CredentialsGenerator credentialsGenerator;

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setCredentialsGenerator(CredentialsGenerator credentialsGenerator) {
        this.credentialsGenerator = credentialsGenerator;
    }

    @Override
    public Trainer createTrainerProfile(String firstName, String lastName, boolean isActive, TrainingType specialization) {
        String username = credentialsGenerator.generateUsername(firstName, lastName,
                candidate -> trainerDao.selectByUsername(candidate).isPresent());
        String password = credentialsGenerator.generatePassword();
        Long id = nextId();
        Trainer trainer = new Trainer(firstName, lastName, username, password, isActive, id, specialization);
        log.info("Created trainer profile for {} {}", firstName, lastName);
        return trainerDao.create(trainer);
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        if (trainerDao.selectById(trainer.getUserId()).isEmpty()) {
            log.warn("Cannot update trainer profile - id={} not found", trainer.getUserId());
            throw new IllegalArgumentException("Trainer not found: " + trainer.getUserId());
        }
        log.info("Updating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());
        return trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> getTrainer(Long id) {
        log.debug("Retrieving trainer profile id={}", id);
        return trainerDao.selectById(id);
    }

    @Override
    public List<Trainer> getAllTrainers() {
        log.debug("Retrieving all trainer profiles");
        return trainerDao.selectAll();
    }

    private Long nextId() {
        return trainerDao.selectAll().stream()
                .mapToLong(Trainer::getUserId)
                .filter(Objects::nonNull)
                .max()
                .orElse(0L) + 1;
    }
}
