package ml.apes.rh.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentation interactive de l'API : /swagger-ui.html (bouton « Authorize » : collez le jeton JWT du login). */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SIGPAP-MALI — Gestion du personnel de l’AP",
        version = "1.0",
        description = "API REST de gestion du personnel de l'Administration Pénitentiaire"
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {}
