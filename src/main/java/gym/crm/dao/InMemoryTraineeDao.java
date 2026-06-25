package gym.crm.dao;

import gym.crm.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTraineeDao implements TraineeDao {
    private Map<Long, Trainee> traineeStorage;

    private static final Logger log = LoggerFactory.getLogger(InMemoryTraineeDao.class);

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.traineeStorage = storage;
    }

    @Override
    public Trainee create(Trainee trainee) {
        traineeStorage.put(trainee.getUserId(), trainee);
        log.info("Created trainee with ID: {}", trainee.getUserId());
        return trainee;
    }

    @Override
    public Optional<Trainee> selectById(Long id) {
        return Optional.ofNullable(traineeStorage.get(id));
    }

    @Override
    public Optional<Trainee> selectByUsername(String username) {
        return traineeStorage.values().stream()
                .filter(trainee -> trainee.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<Trainee> selectAll() {
        return List.copyOf(traineeStorage.values());
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (!traineeStorage.containsKey(trainee.getUserId())) {
            log.warn("Cannot update trainee with ID: {} - record not found", trainee.getUserId());
            throw new IllegalArgumentException("Trainee not found: " + trainee.getUserId());
        }
        traineeStorage.put(trainee.getUserId(), trainee);
        log.info("Updated trainee with ID: {}", trainee.getUserId());
        return trainee;
    }

    @Override
    public void delete(Long id) {
        traineeStorage.remove(id);
        log.info("Deleted trainee with ID: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return traineeStorage.containsKey(id);
    }
}
