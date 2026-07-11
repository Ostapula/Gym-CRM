package gym.crm.controller;

import gym.crm.dto.TraineeDto;
import gym.crm.dto.TrainerSummaryDto;
import gym.crm.exception.ProfileStatusException;
import gym.crm.model.TrainingType;
import gym.crm.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {
    @Mock
    private TraineeService traineeService;
    @InjectMocks
    private TraineeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private TraineeDto traineeDto() {
        TraineeDto dto = new TraineeDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        return dto;
    }

    @Test
    void createReturnsCreatedWithCredentials() throws Exception {
        TraineeDto created = traineeDto();
        created.setPassword("genpass123");
        when(traineeService.createTraineeProfile(any())).thenReturn(created);

        mockMvc.perform(post("/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.password").value("genpass123"));
    }

    @Test
    void createReturnsBadRequestWhenLastNameMissing() throws Exception {
        mockMvc.perform(post("/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\"}"))
                .andExpect(status().isBadRequest());
        verify(traineeService, never()).createTraineeProfile(any());
    }

    @Test
    void getReturnsTraineeWhenFound() throws Exception {
        when(traineeService.getTraineeByUsername("John.Doe")).thenReturn(Optional.of(traineeDto()));

        mockMvc.perform(get("/trainees").param("username", "John.Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Doe"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(traineeService.getTraineeByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/trainees").param("username", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturnsOkWhenUpdated() throws Exception {
        when(traineeService.updateTraineeProfile(any())).thenReturn(Optional.of(traineeDto()));

        mockMvc.perform(put("/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John.Doe\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Doe"));
    }

    @Test
    void updateReturnsBadRequestWhenUsernameMissing() throws Exception {
        mockMvc.perform(put("/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isBadRequest());
        verify(traineeService, never()).updateTraineeProfile(any());
    }

    @Test
    void updateReturnsNotFoundWhenServiceEmpty() throws Exception {
        when(traineeService.updateTraineeProfile(any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsOk() throws Exception {
        mockMvc.perform(delete("/trainees").param("username", "John.Doe"))
                .andExpect(status().isOk());
        verify(traineeService).deleteTraineeProfile("John.Doe");
    }

    @Test
    void assignTrainerReturnsResultingList() throws Exception {
        TrainerSummaryDto summary = new TrainerSummaryDto("Ann.Lee", "Ann", "Lee", TrainingType.CARDIO);
        when(traineeService.updateTraineesTrainerList(eq("John.Doe"), any())).thenReturn(List.of(summary));

        mockMvc.perform(put("/trainees/John.Doe/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Ann.Lee\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Ann.Lee"))
                .andExpect(jsonPath("$[0].specialization").value("CARDIO"));
    }

    @Test
    void assignTrainerReturnsBadRequestWhenListEmpty() throws Exception {
        mockMvc.perform(put("/trainees/John.Doe/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
        verify(traineeService, never()).updateTraineesTrainerList(any(), any());
    }

    @Test
    void getTrainingsReturnsList() throws Exception {
        when(traineeService.getTrainingsByUsername("John.Doe", null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/trainees/John.Doe/trainings"))
                .andExpect(status().isOk());
    }

    @Test
    void setStatusActivates() throws Exception {
        mockMvc.perform(patch("/trainees/John.Doe/status").param("isActive", "true"))
                .andExpect(status().isOk());
        verify(traineeService).activateTraineeProfile("John.Doe");
    }

    @Test
    void setStatusReturnsConflictWhenAlreadyInState() throws Exception {
        doThrow(new ProfileStatusException("already active"))
                .when(traineeService).activateTraineeProfile("John.Doe");

        mockMvc.perform(patch("/trainees/John.Doe/status").param("isActive", "true"))
                .andExpect(status().isConflict());
    }
}
