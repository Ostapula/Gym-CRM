package gym.crm.dto;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "specialization", source = "dto", qualifiedByName = "dtoToSpecialization")
    @Mapping(target = "trainees", source = "traineeIds", qualifiedByName = "idsToTrainees")
    @Mapping(target = "trainings", source = "trainingIds", qualifiedByName = "idsToTrainings")
    Trainer toEntity(TrainerDto dto);

    @Mapping(target = "specializationId", source = "specialization.id")
    @Mapping(target = "specializationType", source = "specialization.type")
    @Mapping(target = "traineeIds", source = "trainees", qualifiedByName = "traineesToIds")
    @Mapping(target = "trainingIds", source = "trainings", qualifiedByName = "trainingsToIds")
    TrainerDto toDto(Trainer trainer);

    List<TrainerDto> toDtoList(List<Trainer> trainers);

    List<Trainer> toEntityList(List<TrainerDto> dtos);

    @Mapping(target = "specialization", source = "specialization.type")
    TrainerSummaryDto toSummary(Trainer trainer);

    List<TrainerSummaryDto> toSummaryList(Set<Trainer> trainers);

    @Named("dtoToSpecialization")
    default TrainingTypeEntity dtoToSpecialization(TrainerDto dto) {
        if (dto.getSpecializationId() == null && dto.getSpecializationType() == null) {
            return null;
        }
        return new TrainingTypeEntity(dto.getSpecializationId(), dto.getSpecializationType());
    }

    @Named("traineesToIds")
    default Set<Long> traineesToIds(Set<Trainee> trainees) {
        if (trainees == null) {
            return null;
        }
        return trainees.stream()
                .map(Trainee::getId)
                .collect(Collectors.toSet());
    }

    @Named("trainingsToIds")
    default Set<Long> trainingsToIds(Set<Training> trainings) {
        if (trainings == null) {
            return null;
        }
        return trainings.stream()
                .map(Training::getId)
                .collect(Collectors.toSet());
    }

    @Named("idsToTrainees")
    default Set<Trainee> idsToTrainees(Set<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> {
                    Trainee trainee = new Trainee();
                    trainee.setId(id);
                    return trainee;
                })
                .collect(Collectors.toSet());
    }

    @Named("idsToTrainings")
    default Set<Training> idsToTrainings(Set<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> {
                    Training training = new Training();
                    training.setId(id);
                    return training;
                })
                .collect(Collectors.toSet());
    }
}
