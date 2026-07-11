package gym.crm.repository;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRepositoryImplTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<TrainingTypeEntity> query;
    @InjectMocks
    private TrainingTypeRepositoryImpl repository;

    private TrainingTypeEntity entity(TrainingType type) {
        TrainingTypeEntity tt = new TrainingTypeEntity();
        tt.setId(1);
        tt.setType(type);
        return tt;
    }

    @Test
    void findByTypeReturnsResult() {
        TrainingTypeEntity tt = entity(TrainingType.CARDIO);
        when(entityManager.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.setParameter("type", TrainingType.CARDIO)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(tt);

        Optional<TrainingTypeEntity> result = repository.findByType(TrainingType.CARDIO);

        assertTrue(result.isPresent());
        assertSame(tt, result.get());
    }

    @Test
    void findByTypeReturnsEmptyOnNoResult() {
        when(entityManager.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.setParameter("type", TrainingType.MOBILITY)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertTrue(repository.findByType(TrainingType.MOBILITY).isEmpty());
    }

    @Test
    void findAllReturnsResultList() {
        when(entityManager.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity(TrainingType.CARDIO), entity(TrainingType.STRENGTH)));

        assertEquals(2, repository.findAll().size());
    }
}
