package gym.crm.facade;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerSummaryDto;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.model.TrainingType;
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
        TraineeDto t = mock(TraineeDto.class);
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
        Optional<TraineeDto> opt = Optional.of(mock(TraineeDto.class));
        when(traineeService.getTraineeByUsername("u")).thenReturn(opt);
        assertSame(opt, facade.getTrainee("u"));
    }

    @Test
    void updateTraineeDelegates() {
        TraineeDto t = mock(TraineeDto.class);
        when(traineeService.updateTraineeProfile(t)).thenReturn(Optional.of(t));
        assertSame(t, facade.updateTrainee(t).orElseThrow());
    }

    @Test
    void changeTraineePasswordDelegates() {
        TraineeDto t = mock(TraineeDto.class);
        when(traineeService.changePasswordTrainee("u", "o", "n")).thenReturn(t);
        assertSame(t, facade.changeTraineePassword("u", "o", "n"));
        verify(traineeService).changePasswordTrainee("u", "o", "n");
    }

    @Test
    void activateTraineeDelegates() {
        facade.activateTrainee("u");
        verify(traineeService).activateTraineeProfile("u");
    }

    @Test
    void deactivateTraineeDelegates() {
        facade.deactivateTrainee("u");
        verify(traineeService).deactivateTraineeProfile("u");
    }

    @Test
    void deleteTraineeDelegates() {
        facade.deleteTrainee("u");
        verify(traineeService).deleteTraineeProfile("u");
    }

    @Test
    void updateTraineeTrainersDelegates() {
        List<TrainerSummaryDto> list = List.of(mock(TrainerSummaryDto.class));
        when(traineeService.updateTraineesTrainerList("john", List.of("ann"))).thenReturn(list);
        assertSame(list, facade.updateTraineeTrainers("john", List.of("ann")));
        verify(traineeService).updateTraineesTrainerList("john", List.of("ann"));
    }

    @Test
    void getAllTraineesDelegates() {
        List<TraineeDto> list = List.of(mock(TraineeDto.class));
        when(traineeService.getAllTrainees()).thenReturn(list);
        assertSame(list, facade.getAllTrainees());
    }

    @Test
    void getTraineeTrainingsDelegatesWithCriteria() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<TrainingDto> list = List.of(mock(TrainingDto.class));
        when(traineeService.getTrainingsByUsername("u", from, to, "Ann Lee", TrainingType.CARDIO))
                .thenReturn(list);

        assertSame(list, facade.getTraineeTrainings("u", from, to, "Ann Lee", TrainingType.CARDIO));
        verify(traineeService).getTrainingsByUsername("u", from, to, "Ann Lee", TrainingType.CARDIO);
    }

    @Test
    void createTrainerDelegates() {
        TrainerDto t = mock(TrainerDto.class);
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
        Optional<TrainerDto> opt = Optional.of(mock(TrainerDto.class));
        when(trainerService.getTrainerByUsername("u")).thenReturn(opt);
        assertSame(opt, facade.getTrainer("u"));
    }

    @Test
    void updateTrainerDelegates() {
        TrainerDto t = mock(TrainerDto.class);
        when(trainerService.updateTrainerProfile(t)).thenReturn(Optional.of(t));
        assertSame(t, facade.updateTrainer(t).orElseThrow());
    }

    @Test
    void changeTrainerPasswordDelegates() {
        TrainerDto t = mock(TrainerDto.class);
        when(trainerService.changePasswordTrainer("u", "o", "n")).thenReturn(t);
        assertSame(t, facade.changeTrainerPassword("u", "o", "n"));
    }

    @Test
    void activateTrainerDelegates() {
        facade.activateTrainer("u");
        verify(trainerService).activateTrainerProfile("u");
    }

    @Test
    void deactivateTrainerDelegates() {
        facade.deactivateTrainer("u");
        verify(trainerService).deactivateTrainerProfile("u");
    }

    @Test
    void getTrainersNotAssignedDelegates() {
        List<TrainerDto> list = List.of(mock(TrainerDto.class));
        when(trainerService.getTrainersNotAssignedToTraineeByUsername("john")).thenReturn(list);
        assertSame(list, facade.getTrainersNotAssignedToTrainee("john"));
    }

    @Test
    void getTrainerTrainingsDelegatesWithCriteria() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<TrainingDto> list = List.of(mock(TrainingDto.class));
        when(trainerService.getTrainingsByUsername("u", from, to, "John Doe")).thenReturn(list);

        assertSame(list, facade.getTrainerTrainings("u", from, to, "John Doe"));
        verify(trainerService).getTrainingsByUsername("u", from, to, "John Doe");
    }

    @Test
    void addTrainingDelegates() {
        AddTrainingRequest request = mock(AddTrainingRequest.class);
        facade.addTraining(request);
        verify(trainingService).createTraining(request);
    }

    @Test
    void getAllTrainingsDelegates() {
        List<TrainingDto> list = List.of(mock(TrainingDto.class));
        when(trainingService.getAllTrainings()).thenReturn(list);
        assertSame(list, facade.getAllTrainings());
    }

    @Test
    void getTrainingTypeDelegates() {
        TrainingTypeEntityDto tt = mock(TrainingTypeEntityDto.class);
        when(trainingTypeService.getByType(TrainingType.STRENGTH)).thenReturn(tt);
        assertSame(tt, facade.getTrainingType(TrainingType.STRENGTH));
    }

    @Test
    void getAllTrainingTypesDelegates() {
        List<TrainingTypeEntityDto> list = List.of(mock(TrainingTypeEntityDto.class));
        when(trainingTypeService.getAll()).thenReturn(list);
        assertSame(list, facade.getAllTrainingTypes());
    }
}
