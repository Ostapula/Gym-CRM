package gym.crm.dto;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(target = "trainings", source = "trainings", qualifiedByName = "idsToTrainings")
    @Mapping(target = "trainers", source = "trainers", qualifiedByName = "idsToTrainers")
    Trainee toEntity(TraineeDto dto);

    @Mapping(target = "trainings", source = "trainings", qualifiedByName = "trainingsToIds")
    @Mapping(target = "trainers", source = "trainers", qualifiedByName = "trainersToIds")
    TraineeDto toDto(Trainee trainee);

    List<TraineeDto> toDtoList(List<Trainee> trainees);

    List<Trainee> toEntityList(List<TraineeDto> dtos);

    @Named("trainingsToIds")
    default Set<Long> trainingsToIds(Set<Training> trainings) {
        if (trainings == null) {
            return null;
        }
        return trainings.stream()
                .map(Training::getId)
                .collect(Collectors.toSet());
    }

    @Named("trainersToIds")
    default Set<Long> trainersToIds(Set<Trainer> trainers) {
        if (trainers == null) {
            return null;
        }
        return trainers.stream()
                .map(Trainer::getId)
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

    @Named("idsToTrainers")
    default Set<Trainer> idsToTrainers(Set<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> {
                    Trainer trainer = new Trainer();
                    trainer.setId(id);
                    return trainer;
                })
                .collect(Collectors.toSet());
    }
}


