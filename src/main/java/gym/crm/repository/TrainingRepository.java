package gym.crm.repository;

import gym.crm.model.Training;

import java.util.List;

public interface TrainingRepository {
    Training save(Training training);
    List<Training> findAllTrainings();
}
