package gym.crm.dao;

import gym.crm.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTrainerDao implements TrainerDao {
    private Map<Long, Trainer> trainerStorage;

    private static final Logger log = LoggerFactory.getLogger(InMemoryTrainerDao.class);

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.trainerStorage = storage;
    }

    @Override
    public Trainer create(Trainer trainer) {
        trainerStorage.put(trainer.getUserId(), trainer);
        log.info("Created trainer with ID: {}", trainer.getUserId());
        return trainer;
    }

    @Override
    public Optional<Trainer> selectById(Long id) {
        return Optional.ofNullable(trainerStorage.get(id));
    }

    @Override
    public Optional<Trainer> selectByUsername(String name) {
        return trainerStorage.values().stream()
                .filter(trainer -> trainer.getUsername().equals(name))
                .findFirst();
    }

    @Override
    public List<Trainer> selectAll() {
        return List.copyOf(trainerStorage.values());
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (!trainerStorage.containsKey(trainer.getUserId())) {
            log.warn("Cannot update trainer with ID: {} - record not found", trainer.getUserId());
            throw new IllegalArgumentException("Trainer not found: " + trainer.getUserId());
        }
        trainerStorage.put(trainer.getUserId(), trainer);
        log.info("Updated trainer with ID: {}", trainer.getUserId());
        return trainer;
    }

    @Override
    public boolean existsById(Long id) {
        return trainerStorage.containsKey(id);
    }
}
