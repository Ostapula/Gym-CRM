package gym.crm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public AuthenticationServiceImpl(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Override
    public boolean matches(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        boolean matched = traineeService.credentialsMatchTrainee(username, password)
                || trainerService.credentialsMatchTrainer(username, password);
        if (!matched) {
            log.info("Authentication failed username={}", username);
        }
        return matched;
    }
}
