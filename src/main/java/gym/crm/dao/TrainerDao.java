package gym.crm.dao;

import gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer create(Trainer trainer);

    Optional<Trainer> selectById(Long id);

    Optional<Trainer> selectByUsername(String name);

    List<Trainer> selectAll();

    Trainer update(Trainer trainer);
}
