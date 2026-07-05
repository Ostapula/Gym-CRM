package gym.crm.repository;

import gym.crm.model.Trainer;
import gym.crm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerRepositoryImpl implements TrainerRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer create(Trainer trainer) {
        entityManager.persist(trainer);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        requireExists(trainer.getId());
        return entityManager.merge(trainer);
    }

    @Override
    public Trainer changePassword(String username, String newPassword) {
        Trainer trainer = findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Failed to change password. Trainer with username " + username + " does not exist."));
        trainer.setPassword(newPassword);
        return entityManager.merge(trainer);
    }

    @Override
    public boolean credentialsMatch(String username, String password) {
        return findByUsername(username)
                .map(trainer -> trainer.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed to check credentials. Trainer with username " + username + " does not exist."));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            Trainer trainer = entityManager.createQuery(
                            "SELECT t FROM Trainer t WHERE t.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void setProfileActiveByUsername(String username, boolean active) {
        Trainer trainer = findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Failed to set profile state. Trainer with username " + username + " does not exist."));

        trainer.setActive(active);
        entityManager.merge(trainer);
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery("SELECT count(t) FROM Trainer t WHERE t.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<Training> findTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        if (findByUsername(username).isEmpty()) {
            throw new IllegalArgumentException("Failed to find trainer with username " + username + " does not exist.");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(training.get("trainer").get("username"), username));
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("date"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("date"), toDate));
        }
        if (traineeName != null) {
            predicates.add(cb.equal(
                    cb.concat(cb.concat(training.get("trainee").get("firstName"), " "),
                            training.get("trainee").get("lastName")),
                    traineeName));
        }

        query.select(training).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Trainer> findTrainersNotAssignedToTraineeByUsername(String traineeUsername) {
        Long traineeCount = entityManager.createQuery(
                        "SELECT count(t) FROM Trainee t WHERE t.username = :traineeUsername", Long.class)
                .setParameter("traineeUsername", traineeUsername)
                .getSingleResult();
        if (traineeCount == 0) {
            throw new IllegalArgumentException("Failed to find trainee with username " + traineeUsername + " does not exist.");
        }

        return entityManager.createQuery(
                        "SELECT t FROM Trainer t WHERE t.id NOT IN " +
                                "(SELECT trn.id FROM Trainee tr JOIN tr.trainers trn WHERE tr.username = :traineeUsername)", Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }

    private void requireExists(Long id) {
        if (!existsById(id)) {
            throw new IllegalArgumentException("Failed to update. Trainer with id " + id + " does not exist.");
        }
    }
}
