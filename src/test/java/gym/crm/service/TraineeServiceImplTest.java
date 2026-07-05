package gym.crm.service;

import gym.crm.dto.TraineeDto;
import gym.crm.dto.TraineeMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.repository.TraineeRepository;
import gym.crm.util.CredentialsGenerator;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @Spy
    private TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @InjectMocks
    private TraineeServiceImpl service;

    private TraineeDto traineeDto(String username, String password, boolean active) {
        TraineeDto t = new TraineeDto("John", "Doe", username, password, active,
                "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        t.setId(1L);
        return t;
    }

    private Trainee trainee(String username, String password, boolean active) {
        Trainee t = new Trainee("John", "Doe", username, password, active,
                "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        t.setId(1L);
        return t;
    }

    @Test
    void createGeneratesCredentialsAndPersists() {
        TraineeDto input = traineeDto(null, null, true);
        when(credentialsGenerator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("John.Doe");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(traineeRepository.create(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        TraineeDto result = service.createTraineeProfile(input);

        assertEquals("John.Doe", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).create(captor.capture());
        Trainee persisted = captor.getValue();
        assertEquals("John.Doe", persisted.getUsername());
        assertEquals("genpass123", persisted.getPassword());
        assertEquals("John", persisted.getFirstName());
        assertTrue(persisted.isActive());
    }

    @Test
    void createRejectsBlankFirstName() {
        TraineeDto input = traineeDto(null, null, true);
        input.setFirstName("  ");
        assertThrows(IllegalArgumentException.class, () -> service.createTraineeProfile(input));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void createRejectsMissingDob() {
        TraineeDto input = traineeDto(null, null, true);
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
        TraineeDto dto = traineeDto("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(traineeRepository.update(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        Optional<TraineeDto> result = service.updateTraineeProfile(dto);

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
        verify(traineeRepository).update(any(Trainee.class));
    }

    @Test
    void updateReturnsEmptyWhenAuthFails() {
        TraineeDto dto = traineeDto("John.Doe", "wrongpass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "realpass", true)));

        assertTrue(service.updateTraineeProfile(dto).isEmpty());
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void changePasswordAuthenticatesWithOldAndStoresNew() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "old", true)));
        when(traineeRepository.changePassword("John.Doe", "new")).thenReturn(trainee("John.Doe", "new", true));

        TraineeDto result = service.changePasswordTrainee("John.Doe", "old", "new");

        assertEquals("new", result.getPassword());
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
    void deactivateSetsInactiveWhenCurrentlyActive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        service.deactivateTraineeProfile("John.Doe", "pass");

        verify(traineeRepository).setProfileActiveByUsername("John.Doe", false);
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
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        Optional<TraineeDto> result = service.getTraineeByUsername("John.Doe", "pass");

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
        assertEquals("123 Main St", result.get().getAddress());
    }

    @Test
    void getByUsernameThrowsWhenPasswordWrong() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        assertThrows(IllegalArgumentException.class, () -> service.getTraineeByUsername("John.Doe", "wrong"));
    }

    @Test
    void getTrainingsDelegatesToRepository() {
        Training training = new Training();
        training.setId(5L);
        training.setName("Morning cardio");
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(traineeRepository.findTrainingsByUsername("John.Doe", null, null, null, TrainingType.CARDIO))
                .thenReturn(List.of(training));

        List<TrainingDto> result = service.getTrainingsByUsername(
                "John.Doe", "pass", null, null, null, TrainingType.CARDIO);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals("Morning cardio", result.get(0).getName());
    }

    @Test
    void getAllTraineesRequiresAuthentication() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(traineeRepository.findAll()).thenReturn(List.of(trainee("a", "p", true)));

        List<TraineeDto> result = service.getAllTrainees("John.Doe", "pass");

        assertEquals(1, result.size());
        assertEquals("a", result.get(0).getUsername());
    }

    @Test
    void updateTrainerListSucceedsWhenAuthenticated() {
        TraineeDto dto = traineeDto("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(traineeRepository.updateTrainerList(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        Optional<TraineeDto> result = service.updateTraineesTrainerList(dto);

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
        verify(traineeRepository).updateTrainerList(any(Trainee.class));
    }

    @Test
    void updateTrainerListReturnsEmptyWhenAuthFails() {
        TraineeDto dto = traineeDto("John.Doe", "wrongpass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "realpass", true)));

        assertTrue(service.updateTraineesTrainerList(dto).isEmpty());
        verify(traineeRepository, never()).updateTrainerList(any());
    }
}
