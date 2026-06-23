package gym.crm.dao;

import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingDaoTest {
    private InMemoryTrainingDao trainingDao;

    @BeforeEach
    public void setUp() {
        trainingDao = new InMemoryTrainingDao();
        trainingDao.setStorage(new HashMap<>());
    }

    private Training sampleTrainer(Long id) {
        return new Training(id, 1L, 1L, "Sample Training", TrainingType.CARDIO,
                LocalDate.of(2026, 6, 20), 60);
    }

    @Test
    void createAndSelectByIdTraining() {
        trainingDao.create(sampleTrainer(1L));
        assertTrue(trainingDao.selectById(1L).isPresent());
        assertEquals("Sample Training", trainingDao.selectById(1L).get().getName());
    }

    @Test
    void selectAllTrainings() {
        trainingDao.create(sampleTrainer(1L));
        trainingDao.create(sampleTrainer(2L));
        assertEquals(2, trainingDao.selectAll().size());
    }
}
