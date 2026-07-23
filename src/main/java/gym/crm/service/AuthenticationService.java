package gym.crm.service;

public interface AuthenticationService {
    String authenticate(String username, String password);
    void logout(String token);
}
