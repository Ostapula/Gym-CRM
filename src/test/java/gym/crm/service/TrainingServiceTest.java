package gym.crm.service;

import gym.crm.dao.TraineeDao;
import gym.crm.dao.TrainerDao;
import gym.crm.dao.TrainingDao;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;

    private TrainingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainingServiceImpl();
        service.setTrainingDao(trainingDao);
        service.setTraineeDao(traineeDao);
        service.setTrainerDao(trainerDao);
    }

    @Test
    void createAssignsNextId() {
        when(trainingDao.selectAll()).thenReturn(List.of(
                new Training(3L, 1L, 1L, "Old", TrainingType.MOBILITY, LocalDate.now(), 30)));
        when(trainingDao.create(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeDao.existsById(1L)).thenReturn(true);
        when(trainerDao.existsById(2L)).thenReturn(true);
        Training created = service.createTraining(1L, 2L, "Cardio Blast",
                TrainingType.CARDIO, LocalDate.of(2024, 5, 1), 45);

        assertEquals(4L, created.getId());
        assertEquals("Cardio Blast", created.getName());
        assertEquals(45, created.getDuration());
    }

    @Test
    void getTrainingDelegatesToDao() {
        Training training = new Training(1L, 1L, 1L, "T", TrainingType.FUNCTIONAL, LocalDate.now(), 60);
        when(trainingDao.selectById(1L)).thenReturn(Optional.of(training));
        assertEquals(training, service.getTraining(1L).orElseThrow());
    }

    @Test
    void getAllTrainingDelegatesToDao() {
        Training training = new Training(1L, 1L, 1L, "T", TrainingType.FUNCTIONAL, LocalDate.now(), 60);
        when(trainingDao.selectAll()).thenReturn(List.of(training));
        assertEquals(List.of(training), service.getAllTrainings());
    }
}
