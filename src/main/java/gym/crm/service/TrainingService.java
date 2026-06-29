package gym.crm.service;

import gym.crm.model.Training;

import java.util.List;

public interface TrainingService {
    Training createTraining(Training training, String trainerUsername, String trainerPassword);
    List<Training> getAllTrainings(String trainerUsername, String trainerPassword);
}

