package gym.crm.dao;

import gym.crm.model.Trainer;
import gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainerDaoTest {
    private InMemoryTrainerDao trainerDao;

    @BeforeEach
    public void setup() {
        trainerDao = new InMemoryTrainerDao();
        trainerDao.setStorage(new HashMap<>());
    }

    private Trainer sampleTrainer(Long id, String username) {
        return new Trainer("John", "Doe", username, "pass123456", true, id, TrainingType.CARDIO);
    }

    @Test
    void createAndSelectById() {
        trainerDao.create(sampleTrainer(1L, "John.Doe"));
        assertTrue(trainerDao.selectById(1L).isPresent());
        assertEquals("John.Doe", trainerDao.selectById(1L).get().getUsername());
    }

    @Test
    void selectByUsername() {
        trainerDao.create(sampleTrainer(1L, "John.Doe"));
        assertTrue(trainerDao.selectByUsername("John.Doe").isPresent());
        assertFalse(trainerDao.selectByUsername("Nobody").isPresent());
    }

    @Test
    void updateReplacesEntry() {
        trainerDao.create(sampleTrainer(1L, "John.Doe"));
        Trainer updated = sampleTrainer(1L, "John.Doe");
        updated.setSpecialization(TrainingType.STRENGTH);
        trainerDao.update(updated);
        assertEquals(TrainingType.STRENGTH, trainerDao.selectById(1L).get().getSpecialization());
    }

    @Test
    void selectAllReturnsEverything() {
        trainerDao.create(sampleTrainer(1L, "John.Doe"));
        trainerDao.create(sampleTrainer(2L, "Jane.Doe"));
        assertEquals(2, trainerDao.selectAll().size());
    }
}
