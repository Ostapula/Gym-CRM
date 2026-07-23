package gym.crm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gym CRM API",
                version = "1.0",
                description = "REST API for managing trainees, trainers and trainings. "
                        + "All endpoints except trainee/trainer registration and login require HTTP Bearer authentication."),
        security = @SecurityRequirement(name = "Bearer Authentication"))
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "A JWT token is required to access this API")
public class OpenApiConfig {
}
