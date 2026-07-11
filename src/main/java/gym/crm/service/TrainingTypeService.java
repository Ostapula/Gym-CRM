package gym.crm.service;

import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.model.TrainingType;

import java.util.List;

public interface TrainingTypeService {
    TrainingTypeEntityDto getByType(TrainingType type);
    List<TrainingTypeEntityDto> getAll();
}
