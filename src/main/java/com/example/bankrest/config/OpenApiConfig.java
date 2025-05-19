package com.example.bankrest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "Bank REST API",
                version     = "1.0",
                description = "API для управления банковскими картами"
        ),
        // Глобально требуем JWT-токен для всех операций
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name         = "bearerAuth",            // то же имя, что в SecurityRequirement
        type         = SecuritySchemeType.HTTP, // HTTP-аутентификация
        scheme       = "bearer",                // схема “Bearer”
        bearerFormat = "JWT"                    // формат — JWT
)
public class OpenApiConfig {

    /**
     * Опционально: дополнительная конфигурация OpenAPI
     * (устанавливает заголовок, описание, версию и т.п.)
     */
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Bank REST API")
                        .version("1.0")
                        .description("API для управления банковскими картами"));
    }
}
