package gym.crm.service;

import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.util.CredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @InjectMocks
    private TrainerServiceImpl service;

    private TrainingTypeEntity type() {
        TrainingTypeEntity tt = new TrainingTypeEntity();
        tt.setId(1);
        tt.setType(TrainingType.CARDIO);
        return tt;
    }

    private Trainer trainer(String username, String password, boolean active) {
        Trainer t = new Trainer("Ann", "Lee", username, password, active, type());
        t.setId(2L);
        t.setTrainees(Set.of());
        t.setTrainings(Set.of());
        return t;
    }

    @Test
    void createGeneratesCredentialsAndPersists() {
        Trainer input = trainer(null, null, true);
        when(credentialsGenerator.generateUsername(eq("Ann"), eq("Lee"), any())).thenReturn("Ann.Lee");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(trainerRepository.create(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        Trainer result = service.createTrainerProfile(input);

        assertEquals("Ann.Lee", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        verify(trainerRepository).create(input);
    }

    @Test
    void createRejectsMissingSpecialization() {
        Trainer input = trainer(null, null, true);
        input.setSpecialization(null);
        assertThrows(NullPointerException.class, () -> service.createTrainerProfile(input));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void credentialsMatchReflectsStoredPassword() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));
        assertTrue(service.credentialsMatchTrainer("Ann.Lee", "pass"));
        assertFalse(service.credentialsMatchTrainer("Ann.Lee", "nope"));
    }

    @Test
    void credentialsMatchTrainerUsernameNotFound() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.empty());

        assertFalse(service.credentialsMatchTrainer("Ann.Lee", "p"));
        verify(trainerRepository).findByUsername("Ann.Lee");
    }

    @Test
    void changePasswordAuthenticatesWithOldAndStoresNew() {
        Trainer t = trainer("Ann.Lee", "old", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t));
        when(trainerRepository.changePassword("Ann.Lee", "new")).thenReturn(t);

        service.changePasswordTrainer("Ann.Lee", "old", "new");

        verify(trainerRepository).changePassword("Ann.Lee", "new");
    }

    @Test
    void changePasswordFailsWhenOldWrong() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "old", true)));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePasswordTrainer("Ann.Lee", "bad", "new"));
        verify(trainerRepository, never()).changePassword(any(), any());
    }

    @Test
    void activateThrowsWhenAlreadyActive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));
        assertThrows(IllegalStateException.class, () -> service.activateTrainerProfile("Ann.Lee", "pass"));
        verify(trainerRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deactivateSetsInactiveWhenCurrentlyActive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));
        service.deactivateTrainerProfile("Ann.Lee", "pass");
        verify(trainerRepository).setProfileActiveByUsername("Ann.Lee", false);
    }

    @Test
    void getTrainersNotAssignedAuthenticatesAgainstTrainee() {
        when(traineeRepository.credentialsMatch("john", "pass")).thenReturn(true);
        when(trainerRepository.findTrainersNotAssignedToTraineeByUsername("john"))
                .thenReturn(List.of(trainer("Ann.Lee", "p", true)));

        assertEquals(1, service.getTrainersNotAssignedToTraineeByUsername("john", "pass").size());
    }

    @Test
    void getTrainersNotAssignedFailsWhenTraineeAuthFails() {
        when(traineeRepository.credentialsMatch("john", "bad")).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrainersNotAssignedToTraineeByUsername("john", "bad"));
        verify(trainerRepository, never()).findTrainersNotAssignedToTraineeByUsername(any());
    }

    @Test
    void updateProfileSucceedsWhenAuthenticated() {
        Trainer t = trainer("Ann.Lee", "pass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t));
        when(trainerRepository.update(t)).thenReturn(t);

        assertSame(t, service.updateTrainerProfile(t));
        verify(trainerRepository).update(t);
    }

    @Test
    void updateProfileReturnsNullWhenAuthFails() {
        Trainer stored = trainer("Ann.Lee", "realpass", true);
        Trainer t = trainer("Ann.Lee", "wrongpass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(stored));

        assertNull(service.updateTrainerProfile(t));
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void getByUsernameReturnsTrainerWhenAuthenticated() {
        Trainer t = trainer("Ann.Lee", "pass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t));

        Optional<Trainer> result = service.getTrainerByUsername("Ann.Lee", "pass");

        assertTrue(result.isPresent());
        assertSame(t, result.get());
    }

    @Test
    void activateSetsActiveWhenCurrentlyInactive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", false)));

        service.activateTrainerProfile("Ann.Lee", "pass");

        verify(trainerRepository).setProfileActiveByUsername("Ann.Lee", true);
    }

    @Test
    void getTrainingsDelegatesToRepository() {
        Trainer t = trainer("Ann.Lee", "pass", true);
        List<Training> list = List.of(mock(Training.class));
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(t));
        when(trainerRepository.findTrainingsByUsername("Ann.Lee", null, null, "John Doe")).thenReturn(list);

        assertEquals(list, service.getTrainingsByUsername("Ann.Lee", "pass", null, null, "John Doe"));
    }
}
