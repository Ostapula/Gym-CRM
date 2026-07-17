package gym.crm.health;

import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainingRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("trainingData")
public class TrainingDataHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;

    public TrainingDataHealthIndicator(TraineeRepository traineeRepository,
                                       TrainingRepository trainingRepository) {
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public Health health() {
        try {
            int trainees = traineeRepository.findAll().size();
            int trainings = trainingRepository.findAllTrainings().size();
            return Health.up()
                    .withDetail("trainees", trainees)
                    .withDetail("trainings", trainings)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("reason", "training data store is unreachable").build();
        }
    }
}
