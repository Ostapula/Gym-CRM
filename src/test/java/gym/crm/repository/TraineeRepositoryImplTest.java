package gym.crm.repository;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeRepositoryImplTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Trainee> traineeQuery;
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
    private TraineeRepositoryImpl repository;

    private Trainee trainee(String password) {
        Trainee t = new Trainee("John", "Doe", "John.Doe", password, true,
                "addr", LocalDate.of(1990, 1, 1), Set.of(), Set.of());
        t.setId(1L);
        return t;
    }

    @Test
    void createPersistsEntity() {
        Trainee t = trainee("pass");

        Trainee result = repository.create(t);

        verify(entityManager).persist(t);
        assertSame(t, result);
    }

    @Test
    void findByUsernameReturnsResult() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);

        Optional<Trainee> result = repository.findByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertSame(t, result.get());
    }

    @Test
    void findByUsernameReturnsEmptyOnNoResult() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "ghost")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertTrue(repository.findByUsername("ghost").isEmpty());
    }

    @Test
    void changePasswordLoadsSetsAndMerges() {
        Trainee t = trainee("old");
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);
        when(entityManager.merge(t)).thenReturn(t);

        repository.changePassword("John.Doe", "new");

        assertEquals("new", t.getPassword());
        verify(entityManager).merge(t);
    }

    @Test
    void existsByIdReturnsTrueWhenCountPositive() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("id", 1L)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(repository.existsById(1L));
    }

    @Test
    void existsByIdReturnsFalseWhenCountZero() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("id", 99L)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(repository.existsById(99L));
    }

    @Test
    void deleteByUsernameRemovesLoadedEntity() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);

        repository.deleteByUsername("John.Doe");

        verify(entityManager).remove(t);
    }

    @Test
    void deleteByUsernameThrowsWhenMissing() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "ghost")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(IllegalArgumentException.class, () -> repository.deleteByUsername("ghost"));
        verify(entityManager, never()).remove(any());
    }

    @Test
    void updateChecksExistenceThenMerges() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("id", 1L)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);
        when(entityManager.merge(t)).thenReturn(t);

        assertSame(t, repository.update(t));
        verify(entityManager).merge(t);
    }

    @Test
    void updateThrowsWhenEntityMissing() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("id", 1L)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> repository.update(t));
        verify(entityManager, never()).merge(any());
    }

    @Test
    void updateTrainerListChecksExistenceThenMerges() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("id", 1L)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);
        when(entityManager.merge(t)).thenReturn(t);

        assertSame(t, repository.updateTrainerList(t));
        verify(entityManager).merge(t);
    }

    @Test
    void credentialsMatchComparesStoredPassword() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);

        assertTrue(repository.credentialsMatch("John.Doe", "pass"));
        assertFalse(repository.credentialsMatch("John.Doe", "wrong"));
    }

    @Test
    void credentialsMatchThrowsWhenAbsent() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "ghost")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(IllegalArgumentException.class, () -> repository.credentialsMatch("ghost", "pass"));
    }

    @Test
    void findAllReturnsResultList() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.getResultList()).thenReturn(List.of(trainee("pass")));

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void setProfileActiveUpdatesFlagAndMerges() {
        Trainee t = trainee("pass");
        t.setActive(false);
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);
        when(entityManager.merge(t)).thenReturn(t);

        repository.setProfileActiveByUsername("John.Doe", true);

        assertTrue(t.isActive());
        verify(entityManager).merge(t);
    }

    @Test
    void findTrainingsByUsernameRunsCriteriaQuery() {
        Trainee t = trainee("pass");
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenReturn(t);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(trainingRoot);
        when(criteriaQuery.select(trainingRoot)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(mock(Training.class)));

        List<Training> result = repository.findTrainingsByUsername(
                "John.Doe", null, null, null, TrainingType.CARDIO);

        assertEquals(1, result.size());
    }

    @Test
    void findTrainingsByUsernameThrowsWhenTraineeMissing() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "ghost")).thenReturn(traineeQuery);
        when(traineeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(IllegalArgumentException.class, () -> repository.findTrainingsByUsername(
                "ghost", null, null, null, TrainingType.CARDIO));
    }
}
