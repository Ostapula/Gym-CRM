package gym.crm.service;

public interface AuthenticationService {
    boolean matches(String username, String password);
}
