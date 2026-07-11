package gym.crm.service;

import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.dto.TrainingTypeMapper;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {
    @Mock
    private TrainingTypeRepository repository;
    @Spy
    private TrainingTypeMapper mapper = Mappers.getMapper(TrainingTypeMapper.class);
    @InjectMocks
    private TrainingTypeServiceImpl service;

    private TrainingTypeEntity entity(TrainingType type) {
        return new TrainingTypeEntity(1, type);
    }

    @Test
    void getByTypeReturnsConfiguredEntity() {
        when(repository.findByType(TrainingType.STRENGTH)).thenReturn(Optional.of(entity(TrainingType.STRENGTH)));

        TrainingTypeEntityDto result = service.getByType(TrainingType.STRENGTH);

        assertEquals(1, result.getId());
        assertEquals(TrainingType.STRENGTH, result.getType());
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

        List<TrainingTypeEntityDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(TrainingType.CARDIO, result.get(0).getType());
    }
}
