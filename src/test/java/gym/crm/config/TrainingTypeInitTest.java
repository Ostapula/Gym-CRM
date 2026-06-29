package gym.crm.config;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeInitTest {
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityTransaction transaction;
    @Mock
    private TypedQuery<Long> countQuery;

    private TrainingTypeInit init;

    @BeforeEach
    void setUp() {
        init = new TrainingTypeInit(entityManagerFactory);
    }

    @Test
    void seedsAllMissingTypesInOneTransaction() {
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("t"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        init.save();

        verify(entityManager, times(TrainingType.values().length)).persist(any(TrainingTypeEntity.class));
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void skipsTypesThatAlreadyExist() {
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("t"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        init.save();

        verify(entityManager, never()).persist(any());
        verify(transaction).commit();
        verify(entityManager).close();
    }
}
