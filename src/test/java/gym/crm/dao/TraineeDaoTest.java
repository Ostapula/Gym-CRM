package gym.crm.dao;

import gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDaoTest {
    private InMemoryTraineeDao dao;

    @BeforeEach
    void setUp() {
        dao = new InMemoryTraineeDao();
        dao.setStorage(new HashMap<>());
    }

    private Trainee sampleTrainee(Long id, String username) {
        return new Trainee("John", "Doe", username, "pass123456", true, id, "Address",
                LocalDate.of(1990, 1, 1));
    }

    @Test
    void createAndSelectById() {
        dao.create(sampleTrainee(1L, "John.Doe"));
        assertTrue(dao.selectById(1L).isPresent());
        assertEquals("John.Doe", dao.selectById(1L).get().getUsername());
    }

    @Test
    void selectByUsername() {
        dao.create(sampleTrainee(1L, "John.Doe"));
        assertTrue(dao.selectByUsername("John.Doe").isPresent());
        assertFalse(dao.selectByUsername("Nobody").isPresent());
    }

    @Test
    void updateReplacesEntry() {
        dao.create(sampleTrainee(1L, "John.Doe"));
        Trainee updated = sampleTrainee(1L, "John.Doe");
        updated.setAddress("Changed");
        dao.update(updated);
        assertEquals("Changed", dao.selectById(1L).get().getAddress());
    }

    @Test
    void deleteRemovesEntry() {
        dao.create(sampleTrainee(1L, "John.Doe"));
        dao.delete(1L);
        assertFalse(dao.selectById(1L).isPresent());
    }

    @Test
    void selectAllReturnsEverything() {
        dao.create(sampleTrainee(1L, "John.Doe"));
        dao.create(sampleTrainee(2L, "Jane.Doe"));
        assertEquals(2, dao.selectAll().size());
    }
}
