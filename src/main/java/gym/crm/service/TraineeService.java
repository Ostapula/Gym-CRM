package gym.crm.service;

import gym.crm.model.Trainee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee createTraineeProfile(String firstName, String lastName, boolean isActive, String address, LocalDate dateOfBirth);

    Trainee updateTrainee(Trainee trainee);

    void deleteTrainee(Long id);

    Optional<Trainee> getTrainee(Long id);

    List<Trainee> getAllTrainees();
}

