package gym.crm.config;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainingTypeInit {
    private final EntityManagerFactory entityManagerFactory;

    public TrainingTypeInit(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void save() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (TrainingType t : TrainingType.values()) {
                Long count = entityManager.createQuery("SELECT count(e) FROM TrainingTypeEntity e " +
                                "WHERE e.type = :t", Long.class)
                        .setParameter("t", t).getSingleResult();
                if (count == 0) {
                    TrainingTypeEntity trainingTypeEntity = new TrainingTypeEntity();
                    trainingTypeEntity.setType(t);
                    entityManager.persist(trainingTypeEntity);
                    log.debug("TrainingTypeEntity {} saved in database: {}", t, trainingTypeEntity);
                }
            }
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }
}
