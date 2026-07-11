package gym.crm.exception;

public abstract class GymCrmException extends RuntimeException {
    protected GymCrmException(String message) {
        super(message);
    }
}
