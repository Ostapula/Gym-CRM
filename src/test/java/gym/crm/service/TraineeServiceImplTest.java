package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.repository.TraineeRepository;
import gym.crm.util.CredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @InjectMocks
    private TraineeServiceImpl service;

    private Trainee trainee(String username, String password, boolean active) {
        Trainee t = new Trainee("John", "Doe", username, password, active,
                "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        t.setId(1L);
        return t;
    }

    @Test
    void createGeneratesCredentialsAndPersists() {
        Trainee input = trainee(null, null, true);
        when(credentialsGenerator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("John.Doe");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(traineeRepository.create(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        Trainee result = service.createTraineeProfile(input);

        assertEquals("John.Doe", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        verify(traineeRepository).create(input);
    }

    @Test
    void createRejectsBlankFirstName() {
        Trainee input = trainee(null, null, true);
        input.setFirstName("  ");
        assertThrows(IllegalArgumentException.class, () -> service.createTraineeProfile(input));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void createRejectsMissingDob() {
        Trainee input = trainee(null, null, true);
        input.setDob(null);
        assertThrows(NullPointerException.class, () -> service.createTraineeProfile(input));
    }

    @Test
    void credentialsMatchTrueWhenPasswordMatches() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        assertTrue(service.credentialsMatchTrainee("John.Doe", "pass"));
    }

    @Test
    void credentialsMatchFalseWhenPasswordDiffers() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        assertFalse(service.credentialsMatchTrainee("John.Doe", "wrong"));
    }

    @Test
    void credentialsMatchFalseWhenNotFound() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertFalse(service.credentialsMatchTrainee("ghost", "pass"));
    }

    @Test
    void updateSucceedsWhenAuthenticated() {
        Trainee t = trainee("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(t));
        when(traineeRepository.update(t)).thenReturn(t);

        assertSame(t, service.updateTraineeProfile(t));
        verify(traineeRepository).update(t);
    }

    @Test
    void updateReturnsNullWhenAuthFails() {
        Trainee stored = trainee("John.Doe", "realpass", true);
        Trainee t = trainee("John.Doe", "wrongpass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(stored));

        assertNull(service.updateTraineeProfile(t));
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void changePasswordAuthenticatesWithOldAndStoresNew() {
        Trainee t = trainee("John.Doe", "old", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(t));
        when(traineeRepository.changePassword("John.Doe", "new")).thenReturn(t);

        service.changePasswordTrainee("John.Doe", "old", "new");

        verify(traineeRepository).changePassword("John.Doe", "new");
    }

    @Test
    void changePasswordFailsWhenOldPasswordWrong() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "old", true)));

        assertThrows(IllegalArgumentException.class,
                () -> service.changePasswordTrainee("John.Doe", "wrong", "new"));
        verify(traineeRepository, never()).changePassword(any(), any());
    }

    @Test
    void activateSetsActiveWhenCurrentlyInactive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", false)));

        service.activateTraineeProfile("John.Doe", "pass");

        verify(traineeRepository).setProfileActiveByUsername("John.Doe", true);
    }

    @Test
    void activateIsNotIdempotentAndThrowsWhenAlreadyActive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        assertThrows(IllegalStateException.class, () -> service.activateTraineeProfile("John.Doe", "pass"));
        verify(traineeRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deactivateThrowsWhenAlreadyInactive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", false)));

        assertThrows(IllegalStateException.class, () -> service.deactivateTraineeProfile("John.Doe", "pass"));
        verify(traineeRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deleteRemovesWhenAuthenticated() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        service.deleteTraineeProfile("John.Doe", "pass");

        verify(traineeRepository).deleteByUsername("John.Doe");
    }

    @Test
    void deleteFailsAuthenticationWhenUserMissing() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteTraineeProfile("ghost", "pass"));
        verify(traineeRepository, never()).deleteByUsername(any());
    }

    @Test
    void getByUsernameReturnsTraineeWhenAuthenticated() {
        Trainee t = trainee("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(t));

        Optional<Trainee> result = service.getTraineeByUsername("John.Doe", "pass");

        assertTrue(result.isPresent());
        assertSame(t, result.get());
    }

    @Test
    void getTrainingsDelegatesToRepository() {
        Trainee t = trainee("John.Doe", "pass", true);
        List<Training> trainings = List.of(mock(Training.class));
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(t));
        when(traineeRepository.findTrainingsByUsername("John.Doe", null, null, null, TrainingType.CARDIO))
                .thenReturn(trainings);

        List<Training> result = service.getTrainingsByUsername(
                "John.Doe", "pass", null, null, null, TrainingType.CARDIO);

        assertEquals(trainings, result);
    }

    @Test
    void getAllTraineesRequiresAuthentication() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(traineeRepository.findAll()).thenReturn(List.of(trainee("a", "p", true)));

        assertEquals(1, service.getAllTrainees("John.Doe", "pass").size());
    }

    @Test
    void deactivateSetsInactiveWhenCurrentlyActive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        service.deactivateTraineeProfile("John.Doe", "pass");

        verify(traineeRepository).setProfileActiveByUsername("John.Doe", false);
    }

    @Test
    void updateTrainerListSucceedsWhenAuthenticated() {
        Trainee t = trainee("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(t));
        when(traineeRepository.updateTrainerList(t)).thenReturn(t);

        assertSame(t, service.updateTraineesTrainerList(t));
        verify(traineeRepository).updateTrainerList(t);
    }

    @Test
    void updateTrainerListReturnsNullWhenAuthFails() {
        Trainee stored = trainee("John.Doe", "realpass", true);
        Trainee t = trainee("John.Doe", "wrongpass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(stored));

        assertNull(service.updateTraineesTrainerList(t));
        verify(traineeRepository, never()).updateTrainerList(any());
    }
}
