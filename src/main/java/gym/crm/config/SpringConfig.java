package gym.crm.config;

import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "gym.crm")
@PropertySource("classpath:application.properties")
public class SpringConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    Map<Long, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    @Bean
    Map<Long, Trainer> trainerStorage() {
        return new HashMap<>();
    }

    @Bean
    Map<Long, Training> trainingStorage() {
        return new HashMap<>();
    }
}
