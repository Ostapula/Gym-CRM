package gym.crm.metrics;

import gym.crm.model.Trainee;
import gym.crm.repository.TraineeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymMetricsRecorderTest {
    @Mock
    private TraineeRepository traineeRepository;

    private MeterRegistry registry;
    private GymMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        recorder = new GymMetricsRecorder(registry, traineeRepository);
    }

    private Trainee trainee(String username, boolean active) {
        return new Trainee("John", "Doe", username, "pass", active,
                "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
    }

    private double counter(String name) {
        return registry.get(name).counter().count();
    }

    @Test
    void recordTraineeRegisteredIncrementsCounter() {
        recorder.recordTraineeRegistered();
        recorder.recordTraineeRegistered();

        assertEquals(2.0, counter("gym.trainee.registrations"));
    }

    @Test
    void recordTrainerRegisteredIncrementsCounter() {
        recorder.recordTrainerRegistered();

        assertEquals(1.0, counter("gym.trainer.registrations"));
    }

    @Test
    void recordTrainingCreatedIncrementsCounter() {
        recorder.recordTrainingCreated();
        recorder.recordTrainingCreated();
        recorder.recordTrainingCreated();

        assertEquals(3.0, counter("gym.training.creations"));
    }

    @Test
    void recordLoginAttemptIncrementsCounterTaggedByOutcome() {
        recorder.recordLoginAttempt(true);
        recorder.recordLoginAttempt(false);
        recorder.recordLoginAttempt(false);

        assertEquals(1.0, registry.get("gym.auth.login").tag("result", "success").counter().count());
        assertEquals(2.0, registry.get("gym.auth.login").tag("result", "failure").counter().count());
    }

    @Test
    void activeTraineesGaugeReflectsActiveCount() {
        when(traineeRepository.findAll()).thenReturn(List.of(
                trainee("a", true),
                trainee("b", false),
                trainee("c", true)));

        assertEquals(2.0, registry.get("gym.trainees.active").gauge().value());
    }

    @Test
    void activeTraineesGaugeIsZeroWhenNoActiveTrainees() {
        when(traineeRepository.findAll()).thenReturn(List.of(trainee("a", false)));

        assertEquals(0.0, registry.get("gym.trainees.active").gauge().value());
    }
}
