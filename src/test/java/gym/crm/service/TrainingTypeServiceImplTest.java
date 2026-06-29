package gym.crm.service;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {
    @Mock
    private TrainingTypeRepository repository;
    @InjectMocks
    private TrainingTypeServiceImpl service;

    private TrainingTypeEntity entity(TrainingType type) {
        TrainingTypeEntity tt = new TrainingTypeEntity();
        tt.setId(1);
        tt.setType(type);
        return tt;
    }

    @Test
    void getByTypeReturnsConfiguredEntity() {
        TrainingTypeEntity tt = entity(TrainingType.STRENGTH);
        when(repository.findByType(TrainingType.STRENGTH)).thenReturn(Optional.of(tt));

        assertSame(tt, service.getByType(TrainingType.STRENGTH));
    }

    @Test
    void getByTypeThrowsWhenNotConfigured() {
        when(repository.findByType(TrainingType.MOBILITY)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getByType(TrainingType.MOBILITY));
    }

    @Test
    void getByTypeRejectsNull() {
        assertThrows(NullPointerException.class, () -> service.getByType(null));
    }

    @Test
    void getAllDelegatesToRepository() {
        when(repository.findAll()).thenReturn(List.of(entity(TrainingType.CARDIO), entity(TrainingType.CIRCUIT)));

        assertEquals(2, service.getAll().size());
    }
}
