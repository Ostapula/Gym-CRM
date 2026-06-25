package gym.crm.service;

import gym.crm.dao.TraineeDao;
import gym.crm.dao.TrainerDao;
import gym.crm.dao.TrainingDao;
import gym.crm.model.Training;
import gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, int trainingDuration) {
        if (!traineeDao.existsById(traineeId)) {
            throw new IllegalArgumentException("Trainee not found: " + traineeId);
        }
        if (!trainerDao.existsById(trainerId)) {
            throw new IllegalArgumentException("Trainer not found: " + trainerId);
        }
        Long id = nextId();
        Training training = new Training(id, trainerId, traineeId, trainingName, trainingType, trainingDate, trainingDuration);
        log.info("Creating training id={} name={}", id, trainingName);
        return trainingDao.create(training);
    }

    @Override
    public Optional<Training> getTraining(Long id) {
        log.debug("Fetching training id={}", id);
        return trainingDao.selectById(id);
    }

    @Override
    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingDao.selectAll();
    }

    private Long nextId() {
        return trainingDao.selectAll().stream()
                .mapToLong(Training::getId)
                .max()
                .orElse(0L) + 1;
    }
}
