package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainerRepository;
import gym.crm.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @InjectMocks
    private TrainingServiceImpl service;

    private TrainingTypeEntity type() {
        TrainingTypeEntity tt = new TrainingTypeEntity();
        tt.setId(1);
        tt.setType(TrainingType.CARDIO);
        return tt;
    }

    private Training training(int duration) {
        Trainer trainer = new Trainer("Ann", "Lee", "Ann.Lee", "p", true, type());
        Trainee trainee = new Trainee("John", "Doe", "John.Doe", "p", true,
                "addr", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        return new Training(null, trainer, trainee, "Morning cardio", type(), LocalDate.now(), duration);
    }

    @Test
    void createTrainingSavesWhenAuthenticatedAndValid() {
        Training t = training(60);
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);
        when(trainingRepository.save(t)).thenReturn(t);

        assertSame(t, service.createTraining(t, "Ann.Lee", "p"));
        verify(trainingRepository).save(t);
    }

    @Test
    void createTrainingFailsWhenTrainerAuthFails() {
        Training t = training(60);
        when(trainerRepository.credentialsMatch("Ann.Lee", "bad")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.createTraining(t, "Ann.Lee", "bad"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTrainingRejectsNonPositiveDuration() {
        Training t = training(0);
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createTraining(t, "Ann.Lee", "p"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getAllTrainingsDelegatesWhenAuthenticated() {
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);
        when(trainingRepository.findAllTrainings()).thenReturn(List.of(training(30)));

        assertEquals(1, service.getAllTrainings("Ann.Lee", "p").size());
    }
}
