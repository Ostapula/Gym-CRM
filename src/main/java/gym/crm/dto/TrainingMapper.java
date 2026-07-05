package gym.crm.dto;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "trainer", source = "trainerId", qualifiedByName = "idToTrainer")
    @Mapping(target = "trainee", source = "traineeId", qualifiedByName = "idToTrainee")
    @Mapping(target = "trainingType", source = "dto", qualifiedByName = "dtoToTrainingType")
    Training toEntity(TrainingDto dto);

    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "traineeId", source = "trainee.id")
    @Mapping(target = "trainingTypeId", source = "trainingType.id")
    @Mapping(target = "trainingType", source = "trainingType.type")
    TrainingDto toDto(Training training);

    List<TrainingDto> toDtoList(List<Training> trainings);

    List<Training> toEntityList(List<TrainingDto> dtos);

    @Named("idToTrainer")
    default Trainer idToTrainer(Long id) {
        if (id == null) {
            return null;
        }
        Trainer trainer = new Trainer();
        trainer.setId(id);
        return trainer;
    }

    @Named("idToTrainee")
    default Trainee idToTrainee(Long id) {
        if (id == null) {
            return null;
        }
        Trainee trainee = new Trainee();
        trainee.setId(id);
        return trainee;
    }

    @Named("dtoToTrainingType")
    default TrainingTypeEntity dtoToTrainingType(TrainingDto dto) {
        if (dto.getTrainingTypeId() == null && dto.getTrainingType() == null) {
            return null;
        }
        return new TrainingTypeEntity(dto.getTrainingTypeId(), dto.getTrainingType());
    }
}
