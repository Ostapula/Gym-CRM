package gym.crm.service;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.User;
import gym.crm.repository.TraineeRepository;
import gym.crm.repository.TrainerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public CustomUserDetailsService(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<Trainee> trainee = traineeRepository.findByUsername(username);
        if (trainee.isPresent()) {
            return buildUserDetails(trainee.get(), "TRAINEE");
        }

        Optional<Trainer> trainer = trainerRepository.findByUsername(username);
        if (trainer.isPresent()) {
            return buildUserDetails(trainer.get(), "TRAINER");
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }

    private UserDetails buildUserDetails(User user, String role) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(role)));
    }
}
