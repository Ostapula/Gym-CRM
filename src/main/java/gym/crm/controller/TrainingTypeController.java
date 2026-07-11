package gym.crm.controller;

import gym.crm.dto.ErrorMessage;
import gym.crm.dto.TrainingTypeEntityDto;
import gym.crm.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Training types", description = "Reference list of training types")
@RestController
@RequestMapping(value = "/training-types", produces = {"application/json"})
public class TrainingTypeController {
    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(summary = "Get training types", description = "Returns the fixed list of training types with their ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types returned", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingTypeEntityDto.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeEntityDto>> getTrainingTypes() {
        return ResponseEntity.ok(trainingTypeService.getAll());
    }
}
