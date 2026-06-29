package gym.crm;

import gym.crm.config.SpringConfig;
import gym.crm.facade.GymFacade;
import gym.crm.model.Trainee;
import gym.crm.model.Trainer;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import gym.crm.model.TrainingTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class)) {
            GymFacade gym = context.getBean(GymFacade.class);

            List<TrainingTypeEntity> trainingTypes = gym.getAllTrainingTypes();
            System.out.println("Training types: " + trainingTypes);
            TrainingTypeEntity strength = gym.getTrainingType(TrainingType.STRENGTH);
            TrainingTypeEntity cardio = gym.getTrainingType(TrainingType.CARDIO);

            Trainee trainee = gym.createTrainee(new Trainee("John", "Doe", null, null, true,
                    "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of()));
            System.out.println("Created trainee: " + trainee.getUsername() + " / " + trainee.getPassword());

            Trainer trainer = gym.createTrainer(new Trainer("Ann", "Lee", null, null, true, strength));
            System.out.println("Created trainer: " + trainer.getUsername() + " / " + trainer.getPassword());

            Training training = gym.addTraining(
                    new Training(null, trainer, trainee, "Morning cardio", cardio, LocalDate.now(), 60),
                    trainer.getUsername(), trainer.getPassword());
            System.out.println("Created training: id=" + training.getId() + " name=" + training.getName());

            System.out.println(gym.getTrainee(trainee.getUsername(), trainee.getPassword()));
            System.out.println(gym.getTrainer(trainer.getUsername(), trainer.getPassword()));
            System.out.println("All trainings: " + gym.getAllTrainings(trainer.getUsername(), trainer.getPassword()));
        }
    }
}
