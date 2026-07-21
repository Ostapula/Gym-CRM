package gym.crm.service;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ValidationException;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    @Mock
    private TraineeRepository traineeRepository;
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @Mock
    private gym.crm.metrics.GymMetricsRecorder metricsRecorder;
    @InjectMocks
    private TrainingServiceImpl service;

    private AddTrainingRequest request(int duration) {
        AddTrainingRequest r = new AddTrainingRequest();
        r.setTraineeUsername("John.Doe");
        r.setTrainerUsername("Ann.Lee");
        r.setTrainingName("Morning cardio");
        r.setTrainingDate(LocalDate.of(2024, 1, 1));
        r.setTrainingDuration(duration);
        return r;
    }

    private Trainer trainer() {
        Trainer trainer = new Trainer("Ann", "Lee", "Ann.Lee", "p", true,
                new TrainingTypeEntity(1, TrainingType.CARDIO));
        trainer.setId(2L);
        return trainer;
    }

    private Trainee trainee() {
        Trainee trainee = new Trainee("John", "Doe", "John.Doe", "p", true,
                "addr", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        trainee.setId(1L);
        return trainee;
    }

    private Training training() {
        return new Training(3L, trainer(), trainee(), "Morning cardio",
                new TrainingTypeEntity(1, TrainingType.CARDIO), LocalDate.of(2024, 1, 1), 30);
    }

    @Test
    void createTrainingResolvesByUsernameAndDerivesTypeFromTrainerSpecialization() {
        Trainer trainer = trainer();
        Trainee trainee = trainee();
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        service.createTraining(request(60));

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        Training saved = captor.getValue();
        assertSame(trainer, saved.getTrainer());
        assertSame(trainee, saved.getTrainee());
        assertEquals(TrainingType.CARDIO, saved.getTrainingType().getType());
        assertEquals("Morning cardio", saved.getName());
        assertEquals(60, saved.getDuration());
        assertEquals(LocalDate.of(2024, 1, 1), saved.getDate());
    }

    @Test
    void createTrainingRejectsNonPositiveDuration() {
        assertThrows(ValidationException.class, () -> service.createTraining(request(0)));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTrainingThrowsWhenTrainerMissing() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createTraining(request(60)));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTrainingThrowsWhenTraineeMissing() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer()));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createTraining(request(60)));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getAllTrainingsDelegates() {
        when(trainingRepository.findAllTrainings()).thenReturn(List.of(training()));

        List<TrainingDto> result = service.getAllTrainings();

        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getId());
        assertEquals(2L, result.getFirst().getTrainerId());
        assertEquals(1L, result.getFirst().getTraineeId());
        assertEquals(TrainingType.CARDIO, result.getFirst().getTrainingType());
    }
}
