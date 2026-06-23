package gym.crm.service;

import gym.crm.model.Training;
import gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training createTraining(Long traineeId, Long trainerId, String trainingName,
                            TrainingType trainingType, LocalDate trainingDate, int trainingDuration);

    Optional<Training> getTraining(Long id);

    List<Training> getAllTrainings();
}

