package gym.crm.repository;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingTypeEntity> findByType(TrainingType type) {
        try {
            TrainingTypeEntity entity = entityManager.createQuery(
                            "FROM TrainingTypeEntity e WHERE e.type = :type", TrainingTypeEntity.class)
                    .setParameter("type", type)
                    .getSingleResult();
            return Optional.of(entity);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<TrainingTypeEntity> findAll() {
        return entityManager.createQuery("FROM TrainingTypeEntity", TrainingTypeEntity.class).getResultList();
    }
}
