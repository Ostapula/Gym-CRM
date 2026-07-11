package gym.crm.controller;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    @Mock
    private TrainingService trainingService;
    @InjectMocks
    private TrainingController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void addTrainingDelegatesToServiceAndReturnsOk() throws Exception {
        mockMvc.perform(post("/trainings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"traineeUsername\":\"John.Doe\",\"trainerUsername\":\"Ann.Lee\","
                                + "\"trainingName\":\"Morning cardio\",\"trainingDate\":\"2024-01-01\","
                                + "\"trainingDuration\":60}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AddTrainingRequest> captor = ArgumentCaptor.forClass(AddTrainingRequest.class);
        verify(trainingService).createTraining(captor.capture());
        AddTrainingRequest request = captor.getValue();
        assertEquals("John.Doe", request.getTraineeUsername());
        assertEquals("Ann.Lee", request.getTrainerUsername());
        assertEquals("Morning cardio", request.getTrainingName());
        assertEquals(LocalDate.of(2024, 1, 1), request.getTrainingDate());
        assertEquals(60, request.getTrainingDuration());
    }
}
