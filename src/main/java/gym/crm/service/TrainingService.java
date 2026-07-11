package gym.crm.service;

import gym.crm.dto.TrainingDto;

import java.util.List;

public interface TrainingService {
    TrainingDto createTraining(TrainingDto trainingDto, String trainerUsername, String trainerPassword);
    List<TrainingDto> getAllTrainings(String trainerUsername, String trainerPassword);
}
