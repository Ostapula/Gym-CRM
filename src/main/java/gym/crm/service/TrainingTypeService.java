package gym.crm.service;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;

import java.util.List;

public interface TrainingTypeService {
    TrainingTypeEntity getByType(TrainingType type);
    List<TrainingTypeEntity> getAll();
}
