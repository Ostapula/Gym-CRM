package gym.crm;

import gym.crm.config.SpringConfig;
import gym.crm.facade.GymFacade;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            log.info("Trainees loaded from file: {}", facade.getAllTrainees().size());
            log.info("Trainers loaded from file: {}", facade.getAllTrainers().size());
            log.info("Trainings loaded from file: {}", facade.getAllTrainings().size());

            Trainee trainee = facade.createTrainee("John", "Doe", true,
                    LocalDate.of(1992, 3, 4), "10 Downing Street");
            log.info("Created trainee with id={}", trainee.getUserId());

            Trainer trainer = facade.createTrainer("Sergey", "Power", true, TrainingType.CARDIO);
            log.info("Created trainer with id={}", trainer.getUserId());

            Training training = facade.createTraining(trainee.getUserId(), trainer.getUserId(),
                    "Intro Session", TrainingType.CARDIO, LocalDate.now(), 90);
            log.info("Created training with id={}", training.getId());

            trainee.setAddress("New Address 42");
            facade.updateTrainee(trainee);
            log.info("Updated trainee id={}", trainee.getUserId());

            facade.deleteTrainee(trainee.getUserId());
            log.info("Trainee present after delete: {}", facade.getTrainee(trainee.getUserId()).isPresent());
        }
    }
}