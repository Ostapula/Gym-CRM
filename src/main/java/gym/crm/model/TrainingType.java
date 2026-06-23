package gym.crm.model;

public enum TrainingType {
    STRENGTH,
    CARDIO,
    FUNCTIONAL,
    MOBILITY,
    CIRCUIT;

    public static TrainingType fromString(String value) {
        return TrainingType.valueOf(value.trim().toUpperCase());
    }
}
