package gym.crm.facade;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.service.TraineeService;
import gym.crm.service.TrainerService;
import gym.crm.service.TrainingService;
import gym.crm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainingTypeService trainingTypeService;
    @InjectMocks
    private GymFacade facade;

    @Test
    void createTraineeDelegates() {
        Trainee t = mock(Trainee.class);
        when(traineeService.createTraineeProfile(t)).thenReturn(t);
        assertSame(t, facade.createTrainee(t));
        verify(traineeService).createTraineeProfile(t);
    }

    @Test
    void authenticateTraineeDelegates() {
        when(traineeService.credentialsMatchTrainee("u", "p")).thenReturn(true);
        assertTrue(facade.authenticateTrainee("u", "p"));
        verify(traineeService).credentialsMatchTrainee("u", "p");
    }

    @Test
    void getTraineeDelegates() {
        Optional<Trainee> opt = Optional.of(mock(Trainee.class));
        when(traineeService.getTraineeByUsername("u", "p")).thenReturn(opt);
        assertSame(opt, facade.getTrainee("u", "p"));
    }

    @Test
    void updateTraineeDelegates() {
        Trainee t = mock(Trainee.class);
        when(traineeService.updateTraineeProfile(t)).thenReturn(t);
        assertSame(t, facade.updateTrainee(t));
    }

    @Test
    void changeTraineePasswordDelegates() {
        Trainee t = mock(Trainee.class);
        when(traineeService.changePasswordTrainee("u", "o", "n")).thenReturn(t);
        assertSame(t, facade.changeTraineePassword("u", "o", "n"));
        verify(traineeService).changePasswordTrainee("u", "o", "n");
    }

    @Test
    void activateTraineeDelegates() {
        facade.activateTrainee("u", "p");
        verify(traineeService).activateTraineeProfile("u", "p");
    }

    @Test
    void deactivateTraineeDelegates() {
        facade.deactivateTrainee("u", "p");
        verify(traineeService).deactivateTraineeProfile("u", "p");
    }

    @Test
    void deleteTraineeDelegates() {
        facade.deleteTrainee("u", "p");
        verify(traineeService).deleteTraineeProfile("u", "p");
    }

    @Test
    void updateTraineeTrainersDelegates() {
        Trainee t = mock(Trainee.class);
        when(traineeService.updateTraineesTrainerList(t)).thenReturn(t);
        assertSame(t, facade.updateTraineeTrainers(t));
    }

    @Test
    void getAllTraineesDelegates() {
        List<Trainee> list = List.of(mock(Trainee.class));
        when(traineeService.getAllTrainees("u", "p")).thenReturn(list);
        assertSame(list, facade.getAllTrainees("u", "p"));
    }

    @Test
    void getTraineeTrainingsDelegatesWithCriteria() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> list = List.of(mock(Training.class));
        when(traineeService.getTrainingsByUsername("u", "p", from, to, "Ann Lee", TrainingType.CARDIO))
                .thenReturn(list);

        assertSame(list, facade.getTraineeTrainings("u", "p", from, to, "Ann Lee", TrainingType.CARDIO));
        verify(traineeService).getTrainingsByUsername("u", "p", from, to, "Ann Lee", TrainingType.CARDIO);
    }

    @Test
    void createTrainerDelegates() {
        Trainer t = mock(Trainer.class);
        when(trainerService.createTrainerProfile(t)).thenReturn(t);
        assertSame(t, facade.createTrainer(t));
    }

    @Test
    void authenticateTrainerDelegates() {
        when(trainerService.credentialsMatchTrainer("u", "p")).thenReturn(true);
        assertTrue(facade.authenticateTrainer("u", "p"));
    }

    @Test
    void getTrainerDelegates() {
        Optional<Trainer> opt = Optional.of(mock(Trainer.class));
        when(trainerService.getTrainerByUsername("u", "p")).thenReturn(opt);
        assertSame(opt, facade.getTrainer("u", "p"));
    }

    @Test
    void updateTrainerDelegates() {
        Trainer t = mock(Trainer.class);
        when(trainerService.updateTrainerProfile(t)).thenReturn(t);
        assertSame(t, facade.updateTrainer(t));
    }

    @Test
    void changeTrainerPasswordDelegates() {
        Trainer t = mock(Trainer.class);
        when(trainerService.changePasswordTrainer("u", "o", "n")).thenReturn(t);
        assertSame(t, facade.changeTrainerPassword("u", "o", "n"));
    }

    @Test
    void activateTrainerDelegates() {
        facade.activateTrainer("u", "p");
        verify(trainerService).activateTrainerProfile("u", "p");
    }

    @Test
    void deactivateTrainerDelegates() {
        facade.deactivateTrainer("u", "p");
        verify(trainerService).deactivateTrainerProfile("u", "p");
    }

    @Test
    void getTrainersNotAssignedDelegates() {
        List<Trainer> list = List.of(mock(Trainer.class));
        when(trainerService.getTrainersNotAssignedToTraineeByUsername("john", "p")).thenReturn(list);
        assertSame(list, facade.getTrainersNotAssignedToTrainee("john", "p"));
    }

    @Test
    void getTrainerTrainingsDelegatesWithCriteria() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> list = List.of(mock(Training.class));
        when(trainerService.getTrainingsByUsername("u", "p", from, to, "John Doe")).thenReturn(list);

        assertSame(list, facade.getTrainerTrainings("u", "p", from, to, "John Doe"));
        verify(trainerService).getTrainingsByUsername("u", "p", from, to, "John Doe");
    }

    // ----- Training delegation -----

    @Test
    void addTrainingDelegates() {
        Training t = mock(Training.class);
        when(trainingService.createTraining(t, "Ann.Lee", "p")).thenReturn(t);
        assertSame(t, facade.addTraining(t, "Ann.Lee", "p"));
        verify(trainingService).createTraining(t, "Ann.Lee", "p");
    }

    @Test
    void getAllTrainingsDelegates() {
        List<Training> list = List.of(mock(Training.class));
        when(trainingService.getAllTrainings("Ann.Lee", "p")).thenReturn(list);
        assertSame(list, facade.getAllTrainings("Ann.Lee", "p"));
    }

    @Test
    void getTrainingTypeDelegates() {
        TrainingTypeEntity tt = mock(TrainingTypeEntity.class);
        when(trainingTypeService.getByType(TrainingType.STRENGTH)).thenReturn(tt);
        assertSame(tt, facade.getTrainingType(TrainingType.STRENGTH));
    }

    @Test
    void getAllTrainingTypesDelegates() {
        List<TrainingTypeEntity> list = List.of(mock(TrainingTypeEntity.class));
        when(trainingTypeService.getAll()).thenReturn(list);
        assertSame(list, facade.getAllTrainingTypes());
    }
}
