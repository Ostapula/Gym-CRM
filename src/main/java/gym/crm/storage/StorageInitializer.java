package gym.crm.storage;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class StorageInitializer implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private static final String DELIMITER = ";";

    private final Resource dataResource;

    private List<Trainee> trainees;
    private List<Trainer> trainers;
    private List<Training> trainings;

    public StorageInitializer(@Value("${storage.init.file}") Resource dataResource) {
        this.dataResource = dataResource;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        switch (beanName) {
            case "traineeStorage" -> {
                ensureParsed();
                Map<Long, Trainee> storage = (Map<Long, Trainee>) bean;
                trainees.forEach(t -> storage.put(t.getUserId(), t));
                log.info("Initialized trainee storage with {} record(s)", trainees.size());
            }
            case "trainerStorage" -> {
                ensureParsed();
                Map<Long, Trainer> storage = (Map<Long, Trainer>) bean;
                trainers.forEach(t -> storage.put(t.getUserId(), t));
                log.info("Initialized trainer storage with {} record(s)", trainers.size());
            }
            case "trainingStorage" -> {
                ensureParsed();
                Map<Long, Training> storage = (Map<Long, Training>) bean;
                trainings.forEach(t -> storage.put(t.getId(), t));
                log.info("Initialized training storage with {} record(s)", trainings.size());
            }
            default -> {
                log.error("Invalid bean name provided: {}", beanName);
            }
        }
        return bean;
    }

    private void ensureParsed() {
        if (trainees != null) {
            return;
        }
        trainees = new ArrayList<>();
        trainers = new ArrayList<>();
        trainings = new ArrayList<>();

        if (dataResource == null || !dataResource.exists()) {
            log.warn("Storage init file '{}' not found - storage will start empty", dataResource);
            return;
        }

        log.info("Loading initial storage data from {}", dataResource);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dataResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                try {
                    parseLine(trimmed);
                } catch (RuntimeException ex) {
                    log.error("Skipping malformed line {} due to parse error: {}", lineNumber, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read storage init file: " + dataResource, ex);
        }
    }

    private void parseLine(String line) {
        String[] parts = line.split(DELIMITER, -1);
        String type = parts[0].trim().toUpperCase();
        switch (type) {
            case "TRAINEE" -> trainees.add(parseTrainee(parts));
            case "TRAINER" -> trainers.add(parseTrainer(parts));
            case "TRAINING" -> trainings.add(parseTraining(parts));
            default -> throw new IllegalArgumentException("Unknown record type: " + type);
        }
    }

    private Trainee parseTrainee(String[] p) {
        return new Trainee(
                p[2].trim(),
                p[3].trim(),
                p[4].trim(),
                p[5].trim(),
                Boolean.parseBoolean(p[6].trim()),
                Long.parseLong(p[1].trim()),
                p[8].trim(),
                LocalDate.parse(p[7].trim()));
    }

    private Trainer parseTrainer(String[] p) {
        return new Trainer(
                p[2].trim(),
                p[3].trim(),
                p[4].trim(),
                p[5].trim(),
                Boolean.parseBoolean(p[6].trim()),
                Long.parseLong(p[1].trim()),
                TrainingType.fromString(p[7]));
    }

    private Training parseTraining(String[] p) {
        return new Training(
                Long.parseLong(p[1].trim()),
                Long.parseLong(p[2].trim()),
                Long.parseLong(p[3].trim()),
                p[4].trim(),
                TrainingType.fromString(p[5]),
                LocalDate.parse(p[6].trim()),
                Integer.parseInt(p[7].trim()));
    }

}
