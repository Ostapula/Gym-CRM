package gym.crm.controller;

import gym.crm.dto.TrainerDto;
import gym.crm.model.TrainingType;
import gym.crm.service.TrainerService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {
    @Mock
    private TrainerService trainerService;
    @InjectMocks
    private TrainerController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private TrainerDto trainerDto() {
        TrainerDto dto = new TrainerDto();
        dto.setUsername("Ann.Lee");
        dto.setFirstName("Ann");
        dto.setLastName("Lee");
        dto.setSpecializationType(TrainingType.CARDIO);
        return dto;
    }

    @Test
    void createReturnsCreatedWithCredentials() throws Exception {
        TrainerDto created = trainerDto();
        created.setPassword("genpass123");
        when(trainerService.createTrainerProfile(any())).thenReturn(created);

        mockMvc.perform(post("/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ann\",\"lastName\":\"Lee\",\"specializationType\":\"CARDIO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Ann.Lee"))
                .andExpect(jsonPath("$.password").value("genpass123"));
    }

    @Test
    void createReturnsBadRequestWhenSpecializationMissing() throws Exception {
        mockMvc.perform(post("/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ann\",\"lastName\":\"Lee\"}"))
                .andExpect(status().isBadRequest());
        verify(trainerService, never()).createTrainerProfile(any());
    }

    @Test
    void getReturnsTrainerWhenFound() throws Exception {
        when(trainerService.getTrainerByUsername("Ann.Lee")).thenReturn(Optional.of(trainerDto()));

        mockMvc.perform(get("/trainers").param("username", "Ann.Lee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Ann.Lee"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(trainerService.getTrainerByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/trainers").param("username", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturnsOkWhenUpdated() throws Exception {
        when(trainerService.updateTrainerProfile(any())).thenReturn(Optional.of(trainerDto()));

        mockMvc.perform(put("/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Ann.Lee\",\"firstName\":\"Ann\",\"lastName\":\"Lee\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Ann.Lee"));
    }

    @Test
    void updateReturnsBadRequestWhenUsernameMissing() throws Exception {
        mockMvc.perform(put("/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ann\",\"lastName\":\"Lee\"}"))
                .andExpect(status().isBadRequest());
        verify(trainerService, never()).updateTrainerProfile(any());
    }

    @Test
    void updateReturnsNotFoundWhenServiceEmpty() throws Exception {
        when(trainerService.updateTrainerProfile(any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"firstName\":\"Ann\",\"lastName\":\"Lee\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotAssignedReturnsList() throws Exception {
        when(trainerService.getTrainersNotAssignedToTraineeByUsername("John.Doe")).thenReturn(List.of());

        mockMvc.perform(get("/trainers/not-assigned").param("username", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainingsReturnsList() throws Exception {
        when(trainerService.getTrainingsByUsername("Ann.Lee", null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/trainers/Ann.Lee/trainings"))
                .andExpect(status().isOk());
    }

    @Test
    void setStatusDeactivates() throws Exception {
        mockMvc.perform(patch("/trainers/Ann.Lee/status").param("isActive", "false"))
                .andExpect(status().isOk());
        verify(trainerService).deactivateTrainerProfile("Ann.Lee");
    }
}
