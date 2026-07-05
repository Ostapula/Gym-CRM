package gym.crm.service;

import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainerMapper;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingMapper;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
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
    private TraineeRepository traineeRepository;
    @Mock
    private CredentialsGenerator credentialsGenerator;
    @Spy
    private TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);
    @Spy
    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);
    @InjectMocks
    private TrainerServiceImpl service;

    private TrainingTypeEntity type() {
        return new TrainingTypeEntity(1, TrainingType.CARDIO);
    }

    private TrainerDto trainerDto(String username, String password, boolean active) {
        return new TrainerDto(2L, "Ann", "Lee", username, password, active,
                1, TrainingType.CARDIO, Set.of(), Set.of());
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
        TrainerDto input = trainerDto(null, null, true);
        when(credentialsGenerator.generateUsername(eq("Ann"), eq("Lee"), any())).thenReturn("Ann.Lee");
        when(credentialsGenerator.generatePassword()).thenReturn("genpass123");
        when(trainerRepository.create(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        TrainerDto result = service.createTrainerProfile(input);

        assertEquals("Ann.Lee", result.getUsername());
        assertEquals("genpass123", result.getPassword());
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).create(captor.capture());
        Trainer persisted = captor.getValue();
        assertEquals("Ann.Lee", persisted.getUsername());
        assertEquals("genpass123", persisted.getPassword());
        assertEquals(1, persisted.getSpecialization().getId());
        assertEquals(TrainingType.CARDIO, persisted.getSpecialization().getType());
    }

    @Test
    void createRejectsMissingSpecialization() {
        TrainerDto input = trainerDto(null, null, true);
        input.setSpecializationId(null);
        input.setSpecializationType(null);
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
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "old", true)));
        when(trainerRepository.changePassword("Ann.Lee", "new")).thenReturn(trainer("Ann.Lee", "new", true));

        TrainerDto result = service.changePasswordTrainer("Ann.Lee", "old", "new");

        assertEquals("new", result.getPassword());
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

        List<TrainerDto> result = service.getTrainersNotAssignedToTraineeByUsername("john", "pass");

        assertEquals(1, result.size());
        assertEquals("Ann.Lee", result.get(0).getUsername());
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
        TrainerDto dto = trainerDto("Ann.Lee", "pass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));
        when(trainerRepository.update(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        TrainerDto result = service.updateTrainerProfile(dto);

        assertNotNull(result);
        assertEquals("Ann.Lee", result.getUsername());
        verify(trainerRepository).update(any(Trainer.class));
    }

    @Test
    void updateProfileReturnsNullWhenAuthFails() {
        TrainerDto dto = trainerDto("Ann.Lee", "wrongpass", true);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "realpass", true)));

        assertNull(service.updateTrainerProfile(dto));
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void getByUsernameReturnsTrainerWhenAuthenticated() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));

        Optional<TrainerDto> result = service.getTrainerByUsername("Ann.Lee", "pass");

        assertTrue(result.isPresent());
        assertEquals("Ann.Lee", result.get().getUsername());
        assertEquals(TrainingType.CARDIO, result.get().getSpecializationType());
    }

    @Test
    void getByUsernameThrowsWhenPasswordWrong() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));

        assertThrows(IllegalArgumentException.class, () -> service.getTrainerByUsername("Ann.Lee", "wrong"));
    }

    @Test
    void activateSetsActiveWhenCurrentlyInactive() {
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", false)));

        service.activateTrainerProfile("Ann.Lee", "pass");

        verify(trainerRepository).setProfileActiveByUsername("Ann.Lee", true);
    }

    @Test
    void getTrainingsDelegatesToRepository() {
        Training training = new Training(5L, trainer("Ann.Lee", "pass", true), null,
                "Morning cardio", type(), LocalDate.of(2024, 1, 1), 60);
        when(trainerRepository.findByUsername("Ann.Lee")).thenReturn(Optional.of(trainer("Ann.Lee", "pass", true)));
        when(trainerRepository.findTrainingsByUsername("Ann.Lee", null, null, "John Doe"))
                .thenReturn(List.of(training));

        List<TrainingDto> result = service.getTrainingsByUsername("Ann.Lee", "pass", null, null, "John Doe");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(2L, result.get(0).getTrainerId());
        assertEquals(TrainingType.CARDIO, result.get(0).getTrainingType());
    }
}
