package gym.crm.controller;

import gym.crm.dto.*;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.model.TrainingType;
import gym.crm.service.TraineeService;
import gym.crm.validation.OnCreate;
import gym.crm.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainee", description = "Trainee registration and profile management")
@RestController
@RequestMapping(value = "/trainees", produces = {"application/json"})
public class TraineeController {
    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Operation(summary = "Register trainee", description = "Creates a trainee profile and returns the generated username and password. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee created", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginDto.class))),
            @ApiResponse(responseCode = "400", description = "First name and last name are required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<LoginDto> createTrainee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainee to register", required = true,
                    content = @Content(schema = @Schema(implementation = TraineeDto.class)))
            @Validated(OnCreate.class) @RequestBody TraineeDto traineeDto) {
        TraineeDto createdTraineeDto = traineeService.createTraineeProfile(traineeDto);
        LoginDto response = new LoginDto();
        response.setUsername(createdTraineeDto.getUsername());
        response.setPassword(createdTraineeDto.getPassword());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get trainee profile", description = "Returns the trainee profile including the assigned trainers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TraineeDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping
    public ResponseEntity<TraineeDto> getTrainee(@RequestParam String username) {
        TraineeDto traineeDto = traineeService.getTraineeByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));
        traineeDto.setTrainings(null);
        return new ResponseEntity<>(traineeDto, HttpStatus.OK);
    }

    @Operation(summary = "Update trainee profile", description = "Updates the editable fields of a trainee identified by username.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee updated", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TraineeDto.class))),
            @ApiResponse(responseCode = "400", description = "Required fields missing", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PutMapping
    public ResponseEntity<TraineeDto> updateTrainee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainee fields to update", required = true,
                    content = @Content(schema = @Schema(implementation = TraineeDto.class)))
            @Validated(OnUpdate.class) @RequestBody TraineeDto traineeDto) {
        TraineeDto updatedTraineeDto = traineeService.updateTraineeProfile(traineeDto)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));
        updatedTraineeDto.setTrainings(null);
        return new ResponseEntity<>(updatedTraineeDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete trainee profile", description = "Hard-deletes the trainee and cascades to their trainings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteTrainee(@RequestParam String username) {
        traineeService.deleteTraineeProfile(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Update trainee's trainer list", description = "Replaces the trainee's assigned trainers and returns the resulting list.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer list updated", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainerSummaryDto.class)))),
            @ApiResponse(responseCode = "400", description = "Trainers list is required or a trainer was not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PutMapping(value = "{username}/trainers", consumes = {"application/json"})
    public ResponseEntity<List<TrainerSummaryDto>> assignTrainer(@PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Usernames of the trainers to assign", required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
            @NotEmpty(message = "Trainers list is required") @RequestBody List<String> trainerUsernames) {
        List<TrainerSummaryDto> trainers = traineeService.updateTraineesTrainerList(username, trainerUsernames);
        return new ResponseEntity<>(trainers, HttpStatus.OK);
    }

    @Operation(summary = "Get trainee trainings", description = "Returns the trainee's trainings, optionally filtered by period, trainer name and training type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings returned", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingDto.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping(value = "{username}/trainings")
    public ResponseEntity<List<TrainingDto>> getTrainings(@PathVariable String username, @RequestParam(required = false) LocalDate fromDate,
                                          @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) String trainerName,
                                          @RequestParam(required = false) TrainingType trainingType) {
        var trainings = traineeService.getTrainingsByUsername(username, fromDate, toDate, trainerName, trainingType);
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

    @Operation(summary = "Activate / de-activate trainee", description = "Sets the trainee active flag. Not idempotent: returns 409 if already in the requested state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "409", description = "Trainee already in the requested state", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PatchMapping(value = "{username}/status")
    public ResponseEntity<Void> setStatus(@PathVariable String username, @RequestParam(name = "isActive") boolean isActive) {
        if (isActive) {
            traineeService.activateTraineeProfile(username);
        } else {
            traineeService.deactivateTraineeProfile(username);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
