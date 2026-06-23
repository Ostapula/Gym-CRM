package gym.crm.service;

import gym.crm.dao.TraineeDao;
import gym.crm.model.Trainee;
import gym.crm.util.CredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceTest {
    @Mock
    private TraineeDao traineeDao;

    private TraineeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TraineeServiceImpl();
        service.setTraineeDao(traineeDao);
        service.setCredentialsGenerator(new CredentialsGenerator());
    }

    @Test
    void createGeneratesCredentialsAndIncrementsId() {
        when(traineeDao.selectAll()).thenReturn(List.of(new Trainee("Mary", "Jane", "Mary.Jane", "pass123456", true, 1L, "Address",
                LocalDate.of(1990, 1, 1))));
        when(traineeDao.selectByUsername("John.Doe")).thenReturn(Optional.empty());
        when(traineeDao.create(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee created = service.createTraineeProfile("John", "Doe", true, "Address", LocalDate.of(1990, 1, 1));

        assertEquals("John.Doe", created.getUsername());
        assertEquals(10, created.getPassword().length());
        assertEquals(2L, created.getUserId());
    }

    @Test
    void updateDelegatesToDao() {
        Trainee trainee = new Trainee("A", "B", "A.B", "x", true, 1L, null, null);
        when(traineeDao.selectById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee updated = service.updateTrainee(trainee);

        assertEquals(trainee, updated);
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateThrowsWhenTraineeMissing() {
        when(traineeDao.selectById(99L)).thenReturn(Optional.empty());
        Trainee trainee = new Trainee("A", "B", "A.B", "x", true, 99L, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.updateTrainee(trainee));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void deleteDelegatesToDao() {
        service.deleteTrainee(7L);
        verify(traineeDao).delete(7L);
    }

    @Test
    void getTraineeDelegatesToDao() {
        Trainee trainee = new Trainee("A", "B", "A.B", "x", true, 1L, null, null);
        when(traineeDao.selectById(1L)).thenReturn(Optional.of(trainee));
        assertEquals(trainee, service.getTrainee(1L).orElseThrow());
    }

    @Test
    void getAllTraineeDelegatesToDao() {
        Trainee trainee = new Trainee("A", "B", "A.B", "x", true, 1L, null, null);
        when(traineeDao.selectAll()).thenReturn(List.of(trainee));
        assertEquals(List.of(trainee), service.getAllTrainees());
    }
}
