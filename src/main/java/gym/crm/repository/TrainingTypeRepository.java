package gym.crm.repository;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository {
    Optional<TrainingTypeEntity> findByType(TrainingType type);
    List<TrainingTypeEntity> findAll();
}
