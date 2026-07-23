package gym.crm.service;

import gym.crm.dto.*;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.ValidationException;
import gym.crm.model.*;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import gym.crm.util.CredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @Spy
    private TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);
    @Spy
    private TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @Mock
    private gym.crm.metrics.GymMetricsRecorder metricsRecorder;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private TraineeServiceImpl service;

    private TraineeDto traineeDto(String username, String password) {
        TraineeDto t = new TraineeDto("John", "Doe", username, password, true,
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
        TraineeDto input = traineeDto(null, null);
        when(credentialsGenerator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("John.Doe");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(passwordEncoder.encode("genpass123")).thenReturn("encoded-genpass123");
        when(traineeRepository.create(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

        TraineeDto result = service.createTraineeProfile(input);

        assertEquals("John.Doe", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).create(captor.capture());
        Trainee persisted = captor.getValue();
        assertEquals("John.Doe", persisted.getUsername());
        assertEquals("encoded-genpass123", persisted.getPassword());
        assertEquals("John", persisted.getFirstName());
        assertTrue(persisted.isActive());
    }

    @Test
    void createRejectsBlankFirstName() {
        TraineeDto input = traineeDto(null, null);
        input.setFirstName("  ");
        assertThrows(ValidationException.class, () -> service.createTraineeProfile(input));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void credentialsMatchTrueWhenPasswordMatches() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(passwordEncoder.matches("pass", "pass")).thenReturn(true);
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
    void updateMutatesLoadedTraineeByUsername() {
        Trainee existing = trainee("John.Doe", "pass", true);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        TraineeDto dto = traineeDto("John.Doe", "pass");
        dto.setFirstName("Johnny");
        dto.setAddress("456 New St");

        Optional<TraineeDto> result = service.updateTraineeProfile(dto);

        assertTrue(result.isPresent());
        assertEquals("Johnny", existing.getFirstName());
        assertEquals("456 New St", existing.getAddress());
        assertEquals("pass", existing.getPassword());
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void updateRejectsMissingUsername() {
        TraineeDto dto = traineeDto(null, null);
        assertThrows(ValidationException.class, () -> service.updateTraineeProfile(dto));
        verify(traineeRepository, never()).findByUsername(any());
    }

    @Test
    void updateThrowsWhenTraineeMissing() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        TraineeDto dto = traineeDto("ghost", "pass");
        assertThrows(EntityNotFoundException.class, () -> service.updateTraineeProfile(dto));
    }

    @Test
    void changePasswordAuthenticatesWithOldAndStoresNew() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "old", true)));
        when(passwordEncoder.matches("old", "old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new");
        when(traineeRepository.changePassword("John.Doe", "new")).thenReturn(trainee("John.Doe", "new", true));

        TraineeDto result = service.changePasswordTrainee("John.Doe", "old", "new");

        assertEquals("new", result.getPassword());
        verify(traineeRepository).changePassword("John.Doe", "new");
    }

    @Test
    void changePasswordFailsWhenOldPasswordWrong() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "old", true)));

        assertThrows(AuthenticationFailedException.class,
                () -> service.changePasswordTrainee("John.Doe", "wrong", "new"));
        verify(traineeRepository, never()).changePassword(any(), any());
    }

    @Test
    void activateSetsActiveWhenCurrentlyInactive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", false)));

        service.activateTraineeProfile("John.Doe");

        verify(traineeRepository).setProfileActiveByUsername("John.Doe", true);
    }

    @Test
    void activateIsNotIdempotentAndThrowsWhenAlreadyActive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        assertThrows(ProfileStatusException.class, () -> service.activateTraineeProfile("John.Doe"));
        verify(traineeRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deactivateThrowsWhenAlreadyInactive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", false)));

        assertThrows(ProfileStatusException.class, () -> service.deactivateTraineeProfile("John.Doe"));
        verify(traineeRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deactivateSetsInactiveWhenCurrentlyActive() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        service.deactivateTraineeProfile("John.Doe");

        verify(traineeRepository).setProfileActiveByUsername("John.Doe", false);
    }

    @Test
    void deleteRemovesWhenTraineeExists() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        service.deleteTraineeProfile("John.Doe");

        verify(traineeRepository).deleteByUsername("John.Doe");
    }

    @Test
    void deleteThrowsWhenTraineeMissing() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteTraineeProfile("ghost"));
        verify(traineeRepository, never()).deleteByUsername(any());
    }

    @Test
    void getByUsernameReturnsTrainee() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));

        Optional<TraineeDto> result = service.getTraineeByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
        assertEquals("123 Main St", result.get().getAddress());
    }

    @Test
    void getByUsernameThrowsWhenMissing() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getTraineeByUsername("ghost"));
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
                "John.Doe", null, null, null, TrainingType.CARDIO);

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
        assertEquals("Morning cardio", result.getFirst().getName());
    }

    @Test
    void getAllTraineesDelegates() {
        when(traineeRepository.findAll()).thenReturn(List.of(trainee("a", "p", true)));

        List<TraineeDto> result = service.getAllTrainees();

        assertEquals(1, result.size());
        assertEquals("a", result.getFirst().getUsername());
    }

    @Test
    void updateTrainerListReplacesTrainersAndReturnsSummaries() {
        Trainee existing = trainee("John.Doe", "pass", true);
        Trainer ann = new Trainer("Ann", "Lee", "Ann.Lee", "p", true,
                new TrainingTypeEntity(1, TrainingType.CARDIO));
        ann.setId(2L);
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(ann));

        List<TrainerSummaryDto> result = service.updateTraineesTrainerList("John.Doe", List.of("Ann.Lee"));

        assertEquals(1, result.size());
        TrainerSummaryDto summary = result.getFirst();
        assertEquals("Ann.Lee", summary.getUsername());
        assertEquals("Ann", summary.getFirstName());
        assertEquals("Lee", summary.getLastName());
        assertEquals(TrainingType.CARDIO, summary.getSpecialization());
        assertEquals(Set.of(ann), existing.getTrainers());
    }

    @Test
    void updateTrainerListThrowsWhenTrainerMissing() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee("John.Doe", "pass", true)));
        when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateTraineesTrainerList("John.Doe", List.of("ghost")));
    }
}
