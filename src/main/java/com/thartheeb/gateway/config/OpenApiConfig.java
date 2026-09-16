package com.thartheeb.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Thartheeb API Gateway", version = "v1",
    description = "Public Swagger UI aggregation for Epic-01 microservice APIs. Internal API groups are intentionally not proxied.",
    contact = @Contact(name = "Thartheeb Engineering"),
    license = @License(name = "Proprietary - Internal Use")))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer",
    bearerFormat = "JWT", description = "Thartheeb user access token")
public class OpenApiConfig {
}
