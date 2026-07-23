package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.ValidationException;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainerRepository;
import gym.crm.repository.TrainingTypeRepository;
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
class TrainerServiceImplTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @Spy
    private TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @Mock
    private gym.crm.metrics.GymMetricsRecorder metricsRecorder;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private TrainerServiceImpl service;

    private TrainingTypeEntity type() {
        return new TrainingTypeEntity(1, TrainingType.CARDIO);
    }

    private TrainerDto trainerDto(String username, String password) {
        return new TrainerDto(2L, "Ann", "Lee", username, password, true,
                1, TrainingType.CARDIO, Set.of(), Set.of());
    }

    private Trainer trainer(String password, boolean active) {
        Trainer t = new Trainer("Ann", "Lee", "Ann.Lee", password, active, type());
        t.setId(2L);
        t.setTrainees(Set.of());
        t.setTrainings(Set.of());
        return t;
    }

    @Test
    void createGeneratesCredentialsAndPersists() {
        TrainerDto input = trainerDto(null, null);
        when(credentialsGenerator.generateUsername(eq("Ann"), eq("Lee"), any())).thenReturn("Ann.Lee");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(passwordEncoder.encode("genpass123")).thenReturn("encoded-genpass123");
        when(trainingTypeRepository.findByType(TrainingType.CARDIO)).thenReturn(Optional.of(type()));
        when(trainerRepository.create(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        TrainerDto result = service.createTrainerProfile(input);

        assertEquals("Ann.Lee", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).create(captor.capture());
        Trainer persisted = captor.getValue();
        assertEquals("Ann.Lee", persisted.getUsername());
        assertEquals("encoded-genpass123", persisted.getPassword());
        assertEquals(1, persisted.getSpecialization().getId());
        assertEquals(TrainingType.CARDIO, persisted.getSpecialization().getType());
    }

    @Test
    void createRejectsMissingSpecialization() {
        TrainerDto input = trainerDto(null, null);
        input.setSpecializationId(null);
        input.setSpecializationType(null);
        assertThrows(ValidationException.class, () -> service.createTrainerProfile(input));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void credentialsMatchReflectsStoredPassword() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", true)));
        when(passwordEncoder.matches("pass", "pass")).thenReturn(true);
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
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("old", true)));
        when(passwordEncoder.matches("old", "old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new");
        when(trainerRepository.changePassword("Ann.Lee", "new")).thenReturn(trainer("new", true));

        TrainerDto result = service.changePasswordTrainer("Ann.Lee", "old", "new");

        assertEquals("new", result.getPassword());
        verify(trainerRepository).changePassword("Ann.Lee", "new");
    }

    @Test
    void changePasswordFailsWhenOldWrong() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("old", true)));
        assertThrows(AuthenticationFailedException.class,
                () -> service.changePasswordTrainer("Ann.Lee", "bad", "new"));
        verify(trainerRepository, never()).changePassword(any(), any());
    }

    @Test
    void activateThrowsWhenAlreadyActive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", true)));
        assertThrows(ProfileStatusException.class, () -> service.activateTrainerProfile("Ann.Lee"));
        verify(trainerRepository, never()).setProfileActiveByUsername(any(), anyBoolean());
    }

    @Test
    void deactivateSetsInactiveWhenCurrentlyActive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", true)));
        service.deactivateTrainerProfile("Ann.Lee");
        verify(trainerRepository).setProfileActiveByUsername("Ann.Lee", false);
    }

    @Test
    void getTrainersNotAssignedDelegates() {
        when(trainerRepository.findTrainersNotAssignedToTraineeByUsername("john"))
                .thenReturn(List.of(trainer("p", true)));

        List<TrainerDto> result = service.getTrainersNotAssignedToTraineeByUsername("john");

        assertEquals(1, result.size());
        assertEquals("Ann.Lee", result.getFirst().getUsername());
    }

    @Test
    void updateProfileMutatesLoadedTrainerByUsername() {
        Trainer existing = trainer("pass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(existing));
        TrainerDto dto = trainerDto("Ann.Lee", "pass");
        dto.setFirstName("Annie");
        dto.setActive(false);

        Optional<TrainerDto> result = service.updateTrainerProfile(dto);

        assertTrue(result.isPresent());
        assertEquals("Annie", existing.getFirstName());
        assertFalse(existing.isActive());
        assertEquals("pass", existing.getPassword());
        assertEquals(TrainingType.CARDIO, existing.getSpecialization().getType());
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void updateProfileRejectsMissingUsername() {
        TrainerDto dto = trainerDto(null, null);
        assertThrows(ValidationException.class, () -> service.updateTrainerProfile(dto));
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void updateProfileThrowsWhenTrainerMissing() {
        when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        TrainerDto dto = trainerDto("ghost", "pass");
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainerProfile(dto));
    }

    @Test
    void getByUsernameReturnsTrainer() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", true)));

        Optional<TrainerDto> result = service.getTrainerByUsername("Ann.Lee");

        assertTrue(result.isPresent());
        assertEquals("Ann.Lee", result.get().getUsername());
        assertEquals(TrainingType.CARDIO, result.get().getSpecializationType());
    }

    @Test
    void getByUsernameThrowsWhenMissing() {
        when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getTrainerByUsername("ghost"));
    }

    @Test
    void activateSetsActiveWhenCurrentlyInactive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", false)));

        service.activateTrainerProfile("Ann.Lee");

        verify(trainerRepository).setProfileActiveByUsername("Ann.Lee", true);
    }

    @Test
    void getTrainingsDelegatesToRepository() {
        Training training = new Training(5L, trainer("pass", true), null,
                "Morning cardio", type(), LocalDate.of(2024, 1, 1), 60);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("pass", true)));
        when(trainerRepository.findTrainingsByUsername("Ann.Lee", null, null, "John Doe"))
                .thenReturn(List.of(training));

        List<TrainingDto> result = service.getTrainingsByUsername("Ann.Lee", null, null, "John Doe");

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
        assertEquals(2L, result.getFirst().getTrainerId());
        assertEquals(TrainingType.CARDIO, result.getFirst().getTrainingType());
    }
}
