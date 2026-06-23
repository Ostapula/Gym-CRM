package gym.crm.facade;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.service.TraineeService;
import gym.crm.service.TrainerService;
import gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void createTraineeDelegatesToServiceWithExpectedArguments() {
        LocalDate dob = LocalDate.of(1995, 6, 10);
        Trainee trainee = new Trainee("John", "Doe", "John.Doe", "pass123456", true, 10L, "NY", dob);
        when(traineeService.createTraineeProfile("John", "Doe", true, "NY", dob)).thenReturn(trainee);

        Trainee created = facade.createTrainee("John", "Doe", true, dob, "NY");

        assertEquals(trainee, created);
        verify(traineeService).createTraineeProfile("John", "Doe", true, "NY", dob);
    }

    @Test
    void createTrainerDelegatesToService() {
        Trainer trainer = new Trainer("Lucy", "Fit", "Lucy.Fit", "pass123456", true, 3L, TrainingType.CARDIO);
        when(trainerService.createTrainerProfile("Lucy", "Fit", true, TrainingType.CARDIO)).thenReturn(trainer);

        Trainer created = facade.createTrainer("Lucy", "Fit", true, TrainingType.CARDIO);

        assertEquals(trainer, created);
        verify(trainerService).createTrainerProfile("Lucy", "Fit", true, TrainingType.CARDIO);
    }

    @Test
    void createTrainingDelegatesToService() {
        LocalDate date = LocalDate.of(2025, 1, 20);
        Training training = new Training(7L, 2L, 1L, "Morning Cardio", TrainingType.CARDIO, date, 45);
        when(trainingService.createTraining(1L, 2L, "Morning Cardio", TrainingType.CARDIO, date, 45))
                .thenReturn(training);

        Training created = facade.createTraining(1L, 2L, "Morning Cardio", TrainingType.CARDIO, date, 45);

        assertEquals(training, created);
        verify(trainingService).createTraining(1L, 2L, "Morning Cardio", TrainingType.CARDIO, date, 45);
    }

    @Test
    void retrievalMethodsDelegateToServices() {
        Trainee trainee = new Trainee("John", "Doe", "John.Doe", "x", true, 1L, "A", LocalDate.of(1990, 1, 1));
        Trainer trainer = new Trainer("Lucy", "Fit", "Lucy.Fit", "y", true, 2L, TrainingType.CARDIO);
        Training training = new Training(3L, 2L, 1L, "Session", TrainingType.CARDIO, LocalDate.of(2025, 1, 1), 30);

        when(traineeService.getTrainee(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.getTrainer(2L)).thenReturn(Optional.of(trainer));
        when(trainingService.getTraining(3L)).thenReturn(Optional.of(training));
        when(traineeService.getAllTrainees()).thenReturn(List.of(trainee));
        when(trainerService.getAllTrainers()).thenReturn(List.of(trainer));
        when(trainingService.getAllTrainings()).thenReturn(List.of(training));

        assertEquals(trainee, facade.getTrainee(1L).orElseThrow());
        assertEquals(trainer, facade.getTrainer(2L).orElseThrow());
        assertEquals(training, facade.getTraining(3L).orElseThrow());
        assertEquals(1, facade.getAllTrainees().size());
        assertEquals(1, facade.getAllTrainers().size());
        assertEquals(1, facade.getAllTrainings().size());

        verify(traineeService).getTrainee(1L);
        verify(trainerService).getTrainer(2L);
        verify(trainingService).getTraining(3L);
        verify(traineeService).getAllTrainees();
        verify(trainerService).getAllTrainers();
        verify(trainingService).getAllTrainings();
    }
}

