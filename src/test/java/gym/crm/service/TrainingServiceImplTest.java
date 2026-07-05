package gym.crm.service;

import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
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
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @InjectMocks
    private TrainingServiceImpl service;

    private TrainingDto trainingDto(int duration) {
        return new TrainingDto(null, 2L, 1L, "Morning cardio",
                1, TrainingType.CARDIO, LocalDate.of(2024, 1, 1), duration);
    }

    private Training training(int duration) {
        Trainer trainer = new Trainer("Ann", "Lee", "Ann.Lee", "p", true,
                new TrainingTypeEntity(1, TrainingType.CARDIO));
        trainer.setId(2L);
        Trainee trainee = new Trainee("John", "Doe", "John.Doe", "p", true,
                "addr", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        trainee.setId(1L);
        return new Training(3L, trainer, trainee, "Morning cardio",
                new TrainingTypeEntity(1, TrainingType.CARDIO), LocalDate.of(2024, 1, 1), duration);
    }

    @Test
    void createTrainingSavesWhenAuthenticatedAndValid() {
        TrainingDto dto = trainingDto(60);
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);
        when(trainingRepository.save(any(Training.class))).thenAnswer(i -> i.getArgument(0));

        TrainingDto result = service.createTraining(dto, "Ann.Lee", "p");

        assertEquals("Morning cardio", result.getName());
        assertEquals(2L, result.getTrainerId());
        assertEquals(1L, result.getTraineeId());
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        Training saved = captor.getValue();
        assertEquals(2L, saved.getTrainer().getId());
        assertEquals(1L, saved.getTrainee().getId());
        assertEquals(TrainingType.CARDIO, saved.getTrainingType().getType());
    }

    @Test
    void createTrainingFailsWhenTrainerAuthFails() {
        TrainingDto dto = trainingDto(60);
        when(trainerRepository.credentialsMatch("Ann.Lee", "bad")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.createTraining(dto, "Ann.Lee", "bad"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTrainingRejectsNonPositiveDuration() {
        TrainingDto dto = trainingDto(0);
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createTraining(dto, "Ann.Lee", "p"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTrainingRejectsMissingTrainer() {
        TrainingDto dto = trainingDto(60);
        dto.setTrainerId(null);
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);

        assertThrows(NullPointerException.class, () -> service.createTraining(dto, "Ann.Lee", "p"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getAllTrainingsDelegatesWhenAuthenticated() {
        when(trainerRepository.credentialsMatch("Ann.Lee", "p")).thenReturn(true);
        when(trainingRepository.findAllTrainings()).thenReturn(List.of(training(30)));

        List<TrainingDto> result = service.getAllTrainings("Ann.Lee", "p");

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(0).getTrainerId());
        assertEquals(1L, result.get(0).getTraineeId());
        assertEquals(TrainingType.CARDIO, result.get(0).getTrainingType());
    }
}
