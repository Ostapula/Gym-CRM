package gym.crm.controller;

import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.UserBlockedException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginReturnsOkWithBearerTokenWhenCredentialsMatch() throws Exception {
        when(authenticationService.authenticate("john", "pass")).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login").param("username", "john").param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer jwt-token"));
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsDoNotMatch() throws Exception {
        when(authenticationService.authenticate("john", "wrong"))
                .thenThrow(new AuthenticationFailedException("Credentials do not match"));

        mockMvc.perform(post("/auth/login").param("username", "john").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTooManyRequestsWhenUserBlocked() throws Exception {
        when(authenticationService.authenticate("john", "pass"))
                .thenThrow(new UserBlockedException("User is temporarily blocked"));

        mockMvc.perform(post("/auth/login").param("username", "john").param("password", "pass"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void loginReturnsBadRequestWhenPasswordMissing() throws Exception {
        mockMvc.perform(post("/auth/login").param("username", "john"))
                .andExpect(status().isBadRequest());
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void logoutReturnsOkAndBlacklistsToken() throws Exception {
        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk());

        verify(authenticationService).logout("jwt-token");
    }

    @Test
    void changePasswordUpdatesTraineeWhenOldCredentialsMatch() throws Exception {
        when(traineeService.credentialsMatchTrainee("john", "old")).thenReturn(true);

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk());

        verify(traineeService).changePasswordTrainee("john", "old", "new");
    }

    @Test
    void changePasswordUpdatesTrainerWhenTraineeDoesNotMatch() throws Exception {
        when(traineeService.credentialsMatchTrainee("john", "old")).thenReturn(false);
        when(trainerService.credentialsMatchTrainer("john", "old")).thenReturn(true);

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk());

        verify(trainerService).changePasswordTrainer("john", "old", "new");
    }

    @Test
    void changePasswordReturnsUnauthorizedWhenNoCredentialsMatch() throws Exception {
        when(traineeService.credentialsMatchTrainee("john", "old")).thenReturn(false);
        when(trainerService.credentialsMatchTrainer("john", "old")).thenReturn(false);

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordReturnsBadRequestWhenFieldMissing() throws Exception {
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"oldPassword\":\"old\"}"))
                .andExpect(status().isBadRequest());
    }
}
