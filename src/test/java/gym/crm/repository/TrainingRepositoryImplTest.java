package gym.crm.repository;

import gym.crm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingRepositoryImplTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Training> trainingQuery;
    @InjectMocks
    private TrainingRepositoryImpl repository;

    @Test
    void savePersistsEntity() {
        Training t = mock(Training.class);
        assertSame(t, repository.save(t));
        verify(entityManager).persist(t);
    }

    @Test
    void findAllTrainingsUsesNamedQuery() {
        when(entityManager.createNamedQuery("Training.findAll", Training.class)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(mock(Training.class)));

        assertEquals(1, repository.findAllTrainings().size());
    }
}
