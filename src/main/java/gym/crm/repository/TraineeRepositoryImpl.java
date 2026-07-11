package gym.crm.repository;

import gym.crm.exception.EntityNotFoundException;
import gym.crm.model.Trainee;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
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
                new EntityNotFoundException("Failed to change password. Trainee with username " + username + " does not exist."));
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Failed to check credentials. Trainee with username " + username + " does not exist."));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            Trainee trainee = entityManager.createQuery(
                            "SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainee);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class)
                .getResultList();
    }

    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new EntityNotFoundException("Failed to delete. Trainee with username " + username + " does not exist."));
        entityManager.remove(trainee);
    }

    @Override
    public void setProfileActiveByUsername(String username, boolean active) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new EntityNotFoundException("Failed to set profile state. Trainee with username " + username + " does not exist."));

        trainee.setActive(active);
        entityManager.merge(trainee);
    }

    @Override
    public List<Training> findTrainingsByUsername(String username, LocalDate fromDate, LocalDate toDate,
                                                  String trainerName, TrainingType trainingType) {
        if (findByUsername(username).isEmpty()) {
            throw new EntityNotFoundException("Failed to find trainee with username " + username + " does not exist.");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(training.get("trainee").get("username"), username));
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("date"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("date"), toDate));
        }
        if (trainerName != null) {
            predicates.add(cb.equal(
                    cb.concat(cb.concat(training.get("trainer").get("firstName"), " "),
                            training.get("trainer").get("lastName")),
                    trainerName));
        }
        if (trainingType != null) {
            predicates.add(cb.equal(training.get("trainingType").get("type"), trainingType));
        }

        query.select(training).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        return entityManager.find(Trainee.class, id) != null;
    }

    private void requireExists(Long id) {
        if (!existsById(id)) {
            throw new EntityNotFoundException("Failed to update. Trainee with id " + id + " does not exist.");
        }
    }
}
