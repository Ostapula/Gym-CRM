package gym.crm.service;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainingTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public TrainingTypeEntity getByType(TrainingType type) {
        Objects.requireNonNull(type, "type is required");
        return trainingTypeRepository.findByType(type).orElseThrow(() ->
                new IllegalArgumentException("Training type " + type + " is not configured."));
    }

    @Override
    public List<TrainingTypeEntity> getAll() {
        return trainingTypeRepository.findAll();
    }
}
