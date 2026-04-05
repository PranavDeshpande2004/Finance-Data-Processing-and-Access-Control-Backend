package com.finance.finance_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3.0 configuration.
 * Adds JWT bearer token support to Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Configures the OpenAPI documentation with
     * project info and JWT security scheme.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Dashboard API")
                        .description(
                                "Finance Data Processing and Access Control System. " +
                                        "Use the Authorize button to enter your JWT token."
                        )
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Finance Team")
                                .email("admin@finance.com")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Enter JWT token. " +
                                                        "Example: eyJhbGciOiJIUzI1NiJ9..."
                                        )
                        )
                );
    }
}
