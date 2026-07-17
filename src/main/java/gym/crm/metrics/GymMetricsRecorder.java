package gym.crm.metrics;

import gym.crm.model.Trainee;
import gym.crm.repository.TraineeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetricsRecorder {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsCreated;
    private final Counter loginSuccess;
    private final Counter loginFailure;

    public GymMetricsRecorder(MeterRegistry registry, TraineeRepository traineeRepository) {
        this.traineeRegistrations = Counter.builder("gym.trainee.registrations")
                .description("Total number of trainee profiles created")
                .register(registry);
        this.trainerRegistrations = Counter.builder("gym.trainer.registrations")
                .description("Total number of trainer profiles created")
                .register(registry);
        this.trainingsCreated = Counter.builder("gym.training.creations")
                .description("Total number of trainings created")
                .register(registry);
        this.loginSuccess = Counter.builder("gym.auth.login")
                .description("Login attempts by outcome")
                .tag("result", "success")
                .register(registry);
        this.loginFailure = Counter.builder("gym.auth.login")
                .description("Login attempts by outcome")
                .tag("result", "failure")
                .register(registry);

        Gauge.builder("gym.trainees.active", traineeRepository, GymMetricsRecorder::countActiveTrainees)
                .description("Number of currently active trainees")
                .register(registry);
    }

    public void recordTraineeRegistered() {
        traineeRegistrations.increment();
    }

    public void recordTrainerRegistered() {
        trainerRegistrations.increment();
    }

    public void recordTrainingCreated() {
        trainingsCreated.increment();
    }

    public void recordLoginAttempt(boolean success) {
        (success ? loginSuccess : loginFailure).increment();
    }

    private static double countActiveTrainees(TraineeRepository repository) {
        return repository.findAll().stream().filter(Trainee::isActive).count();
    }
}
