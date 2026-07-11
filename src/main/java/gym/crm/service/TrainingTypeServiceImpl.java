package gym.crm.service;

import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.dto.TrainingTypeMapper;
import gym.crm.exception.EntityNotFoundException;
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
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository, TrainingTypeMapper trainingTypeMapper) {
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    @Override
    public TrainingTypeEntityDto getByType(TrainingType type) {
        Objects.requireNonNull(type, "type is required");
        TrainingTypeEntity entity = trainingTypeRepository.findByType(type).orElseThrow(() ->
                new EntityNotFoundException("Training type " + type + " is not configured."));
        return trainingTypeMapper.toDto(entity);
    }

    @Override
    public List<TrainingTypeEntityDto> getAll() {
        return trainingTypeMapper.toDtoList(trainingTypeRepository.findAll());
    }
}
