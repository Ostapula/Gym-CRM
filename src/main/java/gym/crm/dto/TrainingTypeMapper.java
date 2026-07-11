package gym.crm.dto;

import gym.crm.model.TrainingTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeEntity toEntity(TrainingTypeEntityDto dto);

    TrainingTypeEntityDto toDto(TrainingTypeEntity entity);

    List<TrainingTypeEntityDto> toDtoList(List<TrainingTypeEntity> entities);

    List<TrainingTypeEntity> toEntityList(List<TrainingTypeEntityDto> dtos);
}
