package gym.crm.dao;

import gym.crm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training create(Training training);

    Optional<Training> selectById(Long id);

    List<Training> selectAll();
}
