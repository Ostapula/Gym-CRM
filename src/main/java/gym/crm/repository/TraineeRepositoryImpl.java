package gym.crm.repository;

import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeRepositoryImpl implements TraineeRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee create(Trainee trainee) {
        entityManager.persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        requireExists(trainee.getId());
        return entityManager.merge(trainee);
    }

    @Override
    public Trainee changePassword(String username, String newPassword) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Failed to change password. Trainee with username " + username + " does not exist."));
        trainee.setPassword(newPassword);
        return entityManager.merge(trainee);
    }

    @Override
    public Trainee updateTrainerList(Trainee trainee) {
        requireExists(trainee.getId());
        return entityManager.merge(trainee);
    }

    @Override
    public boolean credentialsMatch(String username, String password) {
        return findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed to check credentials. Trainee with username " + username + " does not exist."));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            Trainee trainee = entityManager.createQuery(
                            "SELECT t FROM Trainee t " +
                                    "LEFT JOIN FETCH t.trainers " +
                                    "LEFT JOIN FETCH t.trainings " +
                                    "WHERE t.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainee);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery(
                        "SELECT DISTINCT t FROM Trainee t " +
                                "LEFT JOIN FETCH t.trainers " +
                                "LEFT JOIN FETCH t.trainings", Trainee.class)
                .getResultList();
    }

    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Failed to delete. Trainee with username " + username + " does not exist."));
        entityManager.remove(trainee);
    }

    @Override
    public void setProfileActiveByUsername(String username, boolean active) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Failed to set profile state. Trainee with username " + username + " does not exist."));

        trainee.setActive(active);
        entityManager.merge(trainee);
    }

    @Override
    public List<Training> findTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                                  String trainerName, TrainingType trainingType) {
        if (findByUsername(username).isEmpty()) {
            throw new IllegalArgumentException("Failed to find trainee with username " + username + " does not exist.");
        }

        return entityManager.createQuery(
                        "FROM Training t WHERE t.trainee.username = :username " +
                                "AND (:fromDate IS NULL OR t.date >= :fromDate) " +
                                "AND (:toDate IS NULL OR t.date <= :toDate) " +
                                "AND (:trainerName IS NULL OR CONCAT(t.trainer.firstName, ' ', t.trainer.lastName) = :trainerName) " +
                                "AND (:trainingType IS NULL OR t.trainingType.type = :trainingType)", Training.class)
                .setParameter("username", username)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("trainerName", trainerName)
                .setParameter("trainingType", trainingType)
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery("SELECT count(t) FROM Trainee t WHERE t.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    private void requireExists(Long id) {
        if (!existsById(id)) {
            throw new IllegalArgumentException("Failed to update. Trainee with id " + id + " does not exist.");
        }
    }
}
