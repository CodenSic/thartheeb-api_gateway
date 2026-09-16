# api-gateway

Standalone Spring Boot project for Tartheeb.

Purpose: Routes external requests to the appropriate Tartheeb microservice.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Spring Cloud Gateway Server WebFlux
- Spring Boot Actuator

## Run

```powershell
mvn spring-boot:run
```

The application starts on port `8080`. Epic-01 implements service routing, JWT enforcement, denial of internal endpoints, correlation IDs, security headers, and rate limits for registration and authentication traffic.
