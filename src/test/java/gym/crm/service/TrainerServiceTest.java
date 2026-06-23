package gym.crm.service;

import gym.crm.dao.TrainerDao;
import gym.crm.model.Trainer;
import gym.crm.model.TrainingType;
import gym.crm.util.CredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {
    @Mock
    private TrainerDao trainerDao;

    private TrainerServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new TrainerServiceImpl();
        service.setTrainerDao(trainerDao);
        service.setCredentialsGenerator(new CredentialsGenerator());
    }

    @Test
    void createGeneratesCredentialsAndIncrementsId() {
        when(trainerDao.selectAll()).thenReturn(List.of(new Trainer("Mary", "Jane", "Mary.Jane", "pass123456", true, 1L, TrainingType.CARDIO)));
        when(trainerDao.selectByUsername("John.Doe")).thenReturn(Optional.empty());
        when(trainerDao.create(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer created = service.createTrainerProfile("John", "Doe", true, TrainingType.STRENGTH);

        assertEquals("John.Doe", created.getUsername());
        assertEquals(10, created.getPassword().length());
        assertEquals(2L, created.getUserId());
    }

    @Test
    void updateDelegatesToDao() {
        Trainer trainer = new Trainer("A", "B", "A.B", "x", true, 1L, TrainingType.CARDIO);
        when(trainerDao.selectById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.update(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer updated = service.updateTrainer(trainer);

        assertEquals(trainer, updated);
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateThrowsWhenTrainerMissing() {
        when(trainerDao.selectById(99L)).thenReturn(Optional.empty());
        Trainer trainer = new Trainer("A", "B", "A.B", "x", true, 99L, TrainingType.CARDIO);

        assertThrows(IllegalArgumentException.class, () -> service.updateTrainer(trainer));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void getTrainerDelegatesToDao() {
        Trainer trainer = new Trainer("A", "B", "A.B", "x", true, 1L, TrainingType.CARDIO);
        when(trainerDao.selectById(1L)).thenReturn(Optional.of(trainer));
        assertEquals(trainer, service.getTrainer(1L).orElseThrow());
    }

    @Test
    void getAllTrainerDelegatesToDao() {
        Trainer trainer = new Trainer("A", "B", "A.B", "x", true, 1L, TrainingType.CARDIO);
        when(trainerDao.selectAll()).thenReturn(List.of(trainer));
        assertEquals(List.of(trainer), service.getAllTrainers());
    }
}
