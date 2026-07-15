package gym.crm.controller;

import gym.crm.exception.AuthenticationFailedException;
import gym.crm.service.AuthenticationService;
import gym.crm.service.TraineeService;
import gym.crm.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Login and password management")
@RestController
@RequestMapping(value = "/login", produces = {"application/json"})
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public AuthenticationController(AuthenticationService authenticationService,
                                   TraineeService traineeService, TrainerService trainerService) {
        this.authenticationService = authenticationService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "Login", description = "Verifies that the supplied username and password match a trainee or trainer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials are valid"),
            @ApiResponse(responseCode = "400", description = "Username or password missing"),
            @ApiResponse(responseCode = "401", description = "Credentials do not match")
    })
    @GetMapping
    public ResponseEntity<?> login(
            @NotBlank(message = "username is required") @RequestParam(name = "username", required = false) String username,
            @NotBlank(message = "password is required") @RequestParam(name = "password", required = false) String password) {
        if (!authenticationService.matches(username, password)) {
            throw new AuthenticationFailedException("Credentials do not match");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Change password", description = "Changes the password after verifying the current (old) password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Required fields missing"),
            @ApiResponse(responseCode = "401", description = "Old credentials do not match")
    })
    @PutMapping(value = "/change-password", consumes = {"application/json"})
    public ResponseEntity<?> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Username, current and new password", required = true,
                    content = @Content(schema = @Schema(implementation = ChangePassword.class)))
            @Valid @RequestBody ChangePassword changePassword) {
        if (traineeService.credentialsMatchTrainee(changePassword.getUsername(), changePassword.getOldPassword())) {
            traineeService.changePasswordTrainee(changePassword.getUsername(),
                    changePassword.getOldPassword(), changePassword.getNewPassword());
        } else if (trainerService.credentialsMatchTrainer(changePassword.getUsername(), changePassword.getOldPassword())) {
            trainerService.changePasswordTrainer(changePassword.getUsername(),
                    changePassword.getOldPassword(), changePassword.getNewPassword());
        } else {
            throw new AuthenticationFailedException("Credentials do not match");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Getter
    @Setter
    private static class ChangePassword {
        @NotBlank(message = "username is required")
        private String username;
        @NotBlank(message = "oldPassword is required")
        private String oldPassword;
        @NotBlank(message = "newPassword is required")
        private String newPassword;
    }
}
