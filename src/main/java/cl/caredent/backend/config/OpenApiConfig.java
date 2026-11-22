package cl.caredent.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacion Swagger: http://localhost:8080/swagger-ui.html (boton "Authorize" para pegar el JWT). */
@Configuration
public class OpenApiConfig {
    private static final String ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI caredentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Caredent API")
                        .version("1.0.0")
                        .description("API REST de gestión clínica Caredent. Inicia sesión en /api/auth/login, copia el accessToken "
                                + "y pégalo en el botón Authorize para probar los endpoints protegidos."))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
