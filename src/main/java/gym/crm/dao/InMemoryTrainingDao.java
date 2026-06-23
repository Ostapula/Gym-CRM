package gym.crm.dao;

import gym.crm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTrainingDao implements TrainingDao {
    private  Map<Long, Training> trainingStorage;

    private final Logger logger = LoggerFactory.getLogger(InMemoryTrainingDao.class);

    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @Override
    public Training create(Training training) {
        trainingStorage.put(training.getId(), training);
        logger.info("Created training with ID: {}", training.getId());
        return training;
    }

    @Override
    public Optional<Training> selectById(Long id) {
        return Optional.ofNullable(trainingStorage.get(id));
    }

    @Override
    public List<Training> selectAll() {
        return List.copyOf(trainingStorage.values());
    }
}
