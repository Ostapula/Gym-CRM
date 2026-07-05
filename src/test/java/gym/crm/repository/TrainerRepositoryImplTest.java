package gym.crm.repository;

import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryImplTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Trainer> trainerQuery;
    @Mock
    private TypedQuery<Long> longQuery;
    @Mock
    private TypedQuery<Training> trainingQuery;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Training> criteriaQuery;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Root<Training> trainingRoot;
    @InjectMocks
    private TrainerRepositoryImpl repository;

    private Trainer trainer(String username, String password) {
        TrainingTypeEntity tt = new TrainingTypeEntity();
        tt.setId(1);
        tt.setType(TrainingType.CARDIO);
        Trainer t = new Trainer("Ann", "Lee", username, password, true, tt);
        t.setId(2L);
        return t;
    }

    @Test
    void createPersistsEntity() {
        Trainer t = trainer("Ann.Lee", "pass");
        assertSame(t, repository.create(t));
        verify(entityManager).persist(t);
    }

    @Test
    void findByUsernameReturnsResult() {
        Trainer t = trainer("Ann.Lee", "pass");
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Ann.Lee")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenReturn(t);

        assertEquals(Optional.of(t), repository.findByUsername("Ann.Lee"));
    }

    @Test
    void findByUsernameReturnsEmptyOnNoResult() {
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "ghost")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenThrow(new NoResultException());

        assertTrue(repository.findByUsername("ghost").isEmpty());
    }

    @Test
    void changePasswordLoadsSetsAndMerges() {
        Trainer t = trainer("Ann.Lee", "old");
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Ann.Lee")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenReturn(t);
        when(entityManager.merge(t)).thenReturn(t);

        repository.changePassword("Ann.Lee", "new");

        assertEquals("new", t.getPassword());
        verify(entityManager).merge(t);
    }

    @Test
    void updateChecksExistenceThenMerges() {
        Trainer t = trainer("Ann.Lee", "pass");
        when(entityManager.find(Trainer.class, 2L)).thenReturn(t);
        when(entityManager.merge(t)).thenReturn(t);

        assertSame(t, repository.update(t));
        verify(entityManager).merge(t);
    }

    @Test
    void existsByIdReturnsTrueWhenEntityFound() {
        when(entityManager.find(Trainer.class, 2L)).thenReturn(trainer("Ann.Lee", "pass"));

        assertTrue(repository.existsById(2L));
    }

    @Test
    void existsByIdReturnsFalseWhenEntityMissing() {
        when(entityManager.find(Trainer.class, 99L)).thenReturn(null);

        assertFalse(repository.existsById(99L));
    }

    @Test
    void credentialsMatchComparesStoredPassword() {
        Trainer t = trainer("Ann.Lee", "pass");
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Ann.Lee")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenReturn(t);

        assertTrue(repository.credentialsMatch("Ann.Lee", "pass"));
        assertFalse(repository.credentialsMatch("Ann.Lee", "wrong"));
    }

    @Test
    void setProfileActiveUpdatesFlagAndMerges() {
        Trainer t = trainer("Ann.Lee", "pass");
        t.setActive(false);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Ann.Lee")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenReturn(t);
        when(entityManager.merge(t)).thenReturn(t);

        repository.setProfileActiveByUsername("Ann.Lee", true);

        assertTrue(t.isActive());
        verify(entityManager).merge(t);
    }

    @Test
    void findTrainingsByUsernameRunsCriteriaQuery() {
        Trainer t = trainer("Ann.Lee", "pass");
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Ann.Lee")).thenReturn(trainerQuery);
        when(trainerQuery.getSingleResult()).thenReturn(t);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(trainingRoot);
        when(criteriaQuery.select(trainingRoot)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(mock(Training.class)));

        assertEquals(1, repository.findTrainingsByUsername("Ann.Lee", null, null, "John Doe").size());
    }

    @Test
    void findTrainersNotAssignedReturnsList() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("traineeUsername", "john")).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("traineeUsername", "john")).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of(trainer("a", "b")));

        assertEquals(1, repository.findTrainersNotAssignedToTraineeByUsername("john").size());
    }

    @Test
    void findTrainersNotAssignedThrowsWhenTraineeMissing() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("traineeUsername", "ghost")).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> repository.findTrainersNotAssignedToTraineeByUsername("ghost"));
    }
}
