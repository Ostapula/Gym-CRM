package gym.crm;

import gym.crm.config.SpringConfig;
import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainingDto;
import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.facade.GymFacade;
import gym.crm.model.TrainingType;
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

            List<TrainingTypeEntityDto> trainingTypes = gym.getAllTrainingTypes();
            System.out.println("Training types: " + trainingTypes);
            TrainingTypeEntityDto strength = gym.getTrainingType(TrainingType.STRENGTH);
            TrainingTypeEntityDto cardio = gym.getTrainingType(TrainingType.CARDIO);

            TraineeDto trainee = gym.createTrainee(new TraineeDto("John", "Doe", null, null, true,
                    "123 Main St", LocalDate.of(1990, 1, 1), Set.of(), Set.of()));
            System.out.println("Created trainee: " + trainee.getUsername() + " / " + trainee.getPassword());

            TrainerDto trainer = gym.createTrainer(new TrainerDto(null, "Ann", "Lee", null, null, true,
                    strength.getId(), strength.getType(), Set.of(), Set.of()));
            System.out.println("Created trainer: " + trainer.getUsername() + " / " + trainer.getPassword());

            TrainingDto training = gym.addTraining(
                    new TrainingDto(null, trainer.getId(), trainee.getId(), "Morning cardio",
                            cardio.getId(), cardio.getType(), LocalDate.now(), 60),
                    trainer.getUsername(), trainer.getPassword());
            System.out.println("Created training: id=" + training.getId() + " name=" + training.getName());

            System.out.println(gym.getTrainee(trainee.getUsername(), trainee.getPassword()));
            System.out.println(gym.getTrainer(trainer.getUsername(), trainer.getPassword()));
            System.out.println("All trainings: " + gym.getAllTrainings(trainer.getUsername(), trainer.getPassword()));
        }
    }
}
