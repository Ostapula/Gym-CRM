package gym.crm.controller;

import gym.crm.service.AuthenticationService;
import gym.crm.service.TraineeService;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @InjectMocks
    private AuthenticationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginReturnsOkWhenCredentialsMatch() throws Exception {
        when(authenticationService.matches("john", "pass")).thenReturn(true);

        mockMvc.perform(get("/login").param("username", "john").param("password", "pass"))
                .andExpect(status().isOk());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsDoNotMatch() throws Exception {
        when(authenticationService.matches("john", "wrong")).thenReturn(false);

        mockMvc.perform(get("/login").param("username", "john").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsBadRequestWhenPasswordMissing() throws Exception {
        mockMvc.perform(get("/login").param("username", "john"))
                .andExpect(status().isBadRequest());
        verify(authenticationService, never()).matches(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void changePasswordUpdatesTraineeWhenOldCredentialsMatch() throws Exception {
        when(traineeService.credentialsMatchTrainee("john", "old")).thenReturn(true);

        mockMvc.perform(put("/login/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk());

        verify(traineeService).changePasswordTrainee("john", "old", "new");
    }

    @Test
    void changePasswordReturnsUnauthorizedWhenNoCredentialsMatch() throws Exception {
        when(traineeService.credentialsMatchTrainee("john", "old")).thenReturn(false);
        when(trainerService.credentialsMatchTrainer("john", "old")).thenReturn(false);

        mockMvc.perform(put("/login/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordReturnsBadRequestWhenFieldMissing() throws Exception {
        mockMvc.perform(put("/login/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\"}"))
                .andExpect(status().isBadRequest());
    }
}
