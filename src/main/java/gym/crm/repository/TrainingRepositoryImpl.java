package gym.crm.repository;

import gym.crm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrainingRepositoryImpl implements TrainingRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training save(Training training) {
        entityManager.persist(training);
        return training;
    }

    @Override
    public List<Training> findAllTrainings() {
        return entityManager.createNamedQuery("Training.findAll", Training.class).getResultList();
    }

    @Override
    public int countAll() {
        return ((Number) entityManager.createQuery("SELECT COUNT(t) FROM Training t").getSingleResult()).intValue();
    }
}
