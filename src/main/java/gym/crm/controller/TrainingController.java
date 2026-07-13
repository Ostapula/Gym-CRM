package gym.crm.controller;

import gym.crm.dto.AddTrainingRequest;
import gym.crm.dto.ErrorMessage;
import gym.crm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Training", description = "Add trainings")
@RestController
@RequestMapping(value = "/trainings", consumes = {"application/json"}, produces = {"application/json"})
public class TrainingController {
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(summary = "Add training", description = "Creates a training between a trainee and a trainer. The training type is derived from the trainer's specialization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training created"),
            @ApiResponse(responseCode = "400", description = "Required fields missing or trainee/trainer not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping
    public ResponseEntity<?> addTraining(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Training to create", required = true,
                    content = @Content(schema = @Schema(implementation = AddTrainingRequest.class)))
            @RequestBody AddTrainingRequest request) {
        trainingService.createTraining(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
