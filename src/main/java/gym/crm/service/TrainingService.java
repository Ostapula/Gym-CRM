package gym.crm.service;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.TrainingDto;

import java.util.List;

public interface TrainingService {
    void createTraining(AddTrainingRequest request);
    List<TrainingDto> getAllTrainings();
}
