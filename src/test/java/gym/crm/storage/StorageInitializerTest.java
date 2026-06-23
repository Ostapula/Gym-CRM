package gym.crm.storage;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializerTest {

    @Test
    void initializesAllStorageMapsFromResourceData() {
        String csv = String.join("\n",
                "# comment",
                "TRAINEE;1;John;Doe;John.Doe;qheJ1k0pLm;true;1990-05-14;221B Baker Street",
                "TRAINER;2;Lucy;Fit;Lucy.Fit;K3yb0ardZz;true;CARDIO",
                "TRAINING;3;1;2;Morning Cardio;CARDIO;2024-01-16;45");

        StorageInitializer initializer = new StorageInitializer(resource(csv));

        Map<Long, Trainee> traineeStorage = new HashMap<>();
        Map<Long, Trainer> trainerStorage = new HashMap<>();
        Map<Long, Training> trainingStorage = new HashMap<>();

        initializer.postProcessAfterInitialization(traineeStorage, "traineeStorage");
        initializer.postProcessAfterInitialization(trainerStorage, "trainerStorage");
        initializer.postProcessAfterInitialization(trainingStorage, "trainingStorage");

        assertEquals(1, traineeStorage.size());
        assertEquals(1, trainerStorage.size());
        assertEquals(1, trainingStorage.size());

        Trainee trainee = traineeStorage.get(1L);
        assertEquals("John", trainee.getFirstName());
        assertEquals(LocalDate.of(1990, 5, 14), trainee.getDob());

        Trainer trainer = trainerStorage.get(2L);
        assertEquals("Lucy", trainer.getFirstName());
        assertEquals(TrainingType.CARDIO, trainer.getSpecialization());

        Training training = trainingStorage.get(3L);
        assertEquals("Morning Cardio", training.getName());
        assertEquals(45, training.getDuration());
    }

    @Test
    void ignoresMalformedRowsAndUnknownBeanNames() {
        String csv = String.join("\n",
                "TRAINEE;1;John;Doe;John.Doe;qheJ1k0pLm;true;1990-05-14;221B Baker Street",
                "TRAINING;broken-row",
                "UNKNOWN;1;2;3");

        StorageInitializer initializer = new StorageInitializer(resource(csv));

        Map<Long, Trainee> traineeStorage = new HashMap<>();
        initializer.postProcessAfterInitialization(traineeStorage, "traineeStorage");

        assertEquals(1, traineeStorage.size());
        assertTrue(traineeStorage.containsKey(1L));

        Object plainBean = new Object();
        Object result = initializer.postProcessAfterInitialization(plainBean, "someOtherBean");
        assertSame(plainBean, result);
    }

    private ByteArrayResource resource(String data) {
        return new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getDescription() {
                return "test-storage-data";
            }
        };
    }
}

