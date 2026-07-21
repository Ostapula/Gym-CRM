package gym.crm.controller;

import gym.crm.dto.ErrorMessage;
import gym.crm.dto.LoginDto;
import gym.crm.dto.TrainerDto;
import gym.crm.dto.TrainingDto;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.service.TrainerService;
import gym.crm.validation.OnCreate;
import gym.crm.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainer", description = "Trainer registration and profile management")
@RestController
@RequestMapping(value = "/trainers", produces = {"application/json"})
public class TrainerController {
    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(summary = "Register trainer", description = "Creates a trainer profile and returns the generated username and password. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer created", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginDto.class))),
            @ApiResponse(responseCode = "400", description = "First name, last name and specialization are required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping
    public ResponseEntity<LoginDto> createTrainer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainer to register", required = true,
                    content = @Content(schema = @Schema(implementation = TrainerDto.class)))
            @Validated(OnCreate.class) @RequestBody TrainerDto trainerDto) {
        TrainerDto newTrainerDto = trainerService.createTrainerProfile(trainerDto);
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(newTrainerDto.getUsername());
        loginDto.setPassword(newTrainerDto.getPassword());
        return new ResponseEntity<>(loginDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get trainer profile", description = "Returns the trainer profile including the assigned trainees.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TrainerDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping
    public ResponseEntity<TrainerDto> getTrainer(@RequestParam String username) {
        TrainerDto trainerDto = trainerService.getTrainerByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));
        return new ResponseEntity<>(trainerDto, HttpStatus.OK);
    }

    @Operation(summary = "Update trainer profile", description = "Updates the editable fields of a trainer. Specialization is read-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer updated", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TrainerDto.class))),
            @ApiResponse(responseCode = "400", description = "Required fields missing", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PutMapping(consumes = {"application/json"})
    public ResponseEntity<TrainerDto> updateTrainer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainer fields to update", required = true,
                    content = @Content(schema = @Schema(implementation = TrainerDto.class)))
            @Validated(OnUpdate.class) @RequestBody TrainerDto trainerDto) {
        TrainerDto updatedTrainerDto = trainerService.updateTrainerProfile(trainerDto)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));
        return new ResponseEntity<>(updatedTrainerDto, HttpStatus.OK);
    }

    @Operation(summary = "Get trainers not assigned to a trainee", description = "Returns active trainers not currently assigned to the given trainee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainers returned", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainerDto.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping("/not-assigned")
    public ResponseEntity<List<TrainerDto>> getTrainersNotAssignedToTraineeByUsername(@RequestParam String username) {
        return new ResponseEntity<>(trainerService.getTrainersNotAssignedToTraineeByUsername(username), HttpStatus.OK);
    }

    @Operation(summary = "Get trainer trainings", description = "Returns the trainer's trainings, optionally filtered by period and trainee name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings returned", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingDto.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping(value = "{username}/trainings")
    public ResponseEntity<List<TrainingDto>> getTrainings(@PathVariable String username, @RequestParam(required = false) LocalDate fromDate,
                                          @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) String traineeName) {
        var trainings = trainerService.getTrainingsByUsername(username, fromDate, toDate, traineeName);
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

    @Operation(summary = "Activate / de-activate trainer", description = "Sets the trainer active flag. Not idempotent: returns 409 if already in the requested state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "409", description = "Trainer already in the requested state", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PatchMapping(value = "{username}/status")
    public ResponseEntity<Void> setStatus(@PathVariable String username, @RequestParam(name = "isActive") boolean isActive) {
        if (isActive) {
            trainerService.activateTrainerProfile(username);
        } else {
            trainerService.deactivateTrainerProfile(username);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
