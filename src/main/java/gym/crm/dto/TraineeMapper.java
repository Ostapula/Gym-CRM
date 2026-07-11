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
    @Mapping(target = "trainers", source = "trainers", qualifiedByName = "usernamesToTrainers")
    Trainee toEntity(TraineeDto dto);

    @Mapping(target = "trainings", source = "trainings", qualifiedByName = "trainingsToIds")
    @Mapping(target = "trainers", source = "trainers", qualifiedByName = "trainersToUsernames")
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

    @Named("trainersToUsernames")
    default Set<String> trainersToUsernames(Set<Trainer> trainers) {
        if (trainers == null) {
            return null;
        }
        return trainers.stream()
                .map(Trainer::getUsername)
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

    @Named("usernamesToTrainers")
    default Set<Trainer> usernamesToTrainers(Set<String> usernames) {
        if (usernames == null) {
            return null;
        }
        return usernames.stream()
                .map(username -> {
                    Trainer trainer = new Trainer();
                    trainer.setUsername(username);
                    return trainer;
                })
                .collect(Collectors.toSet());
    }
}


