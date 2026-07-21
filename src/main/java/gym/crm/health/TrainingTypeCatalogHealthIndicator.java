package gym.crm.health;

import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import gym.crm.repository.TrainingTypeRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("trainingTypeCatalog")
public class TrainingTypeCatalogHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeCatalogHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        try {
            Set<TrainingType> present = trainingTypeRepository.findAll().stream()
                    .map(TrainingTypeEntity::getType)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(TrainingType.class)));
            List<TrainingType> missing = Arrays.stream(TrainingType.values())
                    .filter(type -> !present.contains(type))
                    .toList();

            Health.Builder builder = missing.isEmpty() ? Health.up() : Health.down();
            return builder
                    .withDetail("expected", TrainingType.values().length)
                    .withDetail("present", present.size())
                    .withDetail("missing", missing)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("reason", "training type catalog is unreachable").build();
        }
    }
}
