package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @InjectMocks
    private CustomUserDetailsService service;

    private Trainee trainee(boolean active) {
        Trainee t = new Trainee("John", "Doe", "John.Doe", "secret", active,
                "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        t.setId(1L);
        return t;
    }

    private Trainer trainer() {
        Trainer t = new Trainer("Ann", "Lee", "Ann.Lee", "pass", true,
                new TrainingTypeEntity(1, TrainingType.CARDIO));
        t.setId(2L);
        return t;
    }

    private List<String> authorities(UserDetails details) {
        return details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void loadsTraineeWithTraineeAuthority() {
        when(traineeRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainee(true)));

        UserDetails details = service.loadUserByUsername("John.Doe");

        assertEquals("John.Doe", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.isEnabled());
        assertEquals(List.of("TRAINEE"), authorities(details));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void loadsTrainerWhenNotATrainee() {
        when(traineeRepository.findByUsername("Ann.Lee")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Ann.Lee"))
                .thenReturn(Optional.of(trainer()));

        UserDetails details = service.loadUserByUsername("Ann.Lee");

        assertEquals("Ann.Lee", details.getUsername());
        assertEquals("pass", details.getPassword());
        assertEquals(List.of("TRAINER"), authorities(details));
    }

    @Test
    void traineeTakesPrecedenceOverTrainer() {
        when(traineeRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainee(true)));

        UserDetails details = service.loadUserByUsername("John.Doe");

        assertEquals(List.of("TRAINEE"), authorities(details));
        verify(trainerRepository, never()).findByUsername("John.Doe");
    }

    @Test
    void inactiveUserIsDisabled() {
        when(traineeRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainee(false)));

        UserDetails details = service.loadUserByUsername("John.Doe");

        assertFalse(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    void throwsWhenUserNotFound() {
        when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }
}
