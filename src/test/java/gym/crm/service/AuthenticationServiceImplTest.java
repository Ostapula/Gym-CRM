package gym.crm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void matchesWhenTraineeCredentialsMatch() {
        when(traineeService.credentialsMatchTrainee("john", "pass")).thenReturn(true);
        assertTrue(service.matches("john", "pass"));
    }

    @Test
    void matchesWhenTrainerCredentialsMatch() {
        when(traineeService.credentialsMatchTrainee("ann", "pass")).thenReturn(false);
        when(trainerService.credentialsMatchTrainer("ann", "pass")).thenReturn(true);
        assertTrue(service.matches("ann", "pass"));
    }

    @Test
    void doesNotMatchWhenNeitherMatches() {
        when(traineeService.credentialsMatchTrainee("ghost", "pass")).thenReturn(false);
        when(trainerService.credentialsMatchTrainer("ghost", "pass")).thenReturn(false);
        assertFalse(service.matches("ghost", "pass"));
    }

    @Test
    void doesNotMatchWhenCredentialsNullAndSkipsServices() {
        assertFalse(service.matches(null, "pass"));
        assertFalse(service.matches("john", null));
        verifyNoInteractions(traineeService, trainerService);
    }
}
