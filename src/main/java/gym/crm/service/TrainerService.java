package gym.crm.service;

import gym.crm.model.Trainer;
import gym.crm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer createTrainerProfile(String firstName, String lastName, boolean isActive,
                                 TrainingType specialization);

    Trainer updateTrainer(Trainer trainer);

    Optional<Trainer> getTrainer(Long id);

    List<Trainer> getAllTrainers();
}
