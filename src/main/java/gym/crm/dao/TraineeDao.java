package gym.crm.dao;

import gym.crm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee create(Trainee trainee);

    Optional<Trainee> selectById(Long id);

    Optional<Trainee> selectByUsername(String username);

    List<Trainee> selectAll();

    Trainee update(Trainee trainee);

    void delete(Long id);
}
