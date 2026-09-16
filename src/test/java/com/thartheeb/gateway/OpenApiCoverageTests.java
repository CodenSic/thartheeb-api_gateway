package com.thartheeb.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class OpenApiCoverageTests {
    @Autowired
    private Environment environment;

    @Test
    void configuresGatewaySwaggerAggregation() {
        assertThat(environment.getProperty("springdoc.swagger-ui.path"))
                .isEqualTo("/swagger-ui.html");
        assertThat(environment.getProperty("springdoc.swagger-ui.urls[0].url"))
                .isEqualTo("/openapi/authentication");
        assertThat(environment.getProperty("springdoc.swagger-ui.urls[1].url"))
                .isEqualTo("/openapi/vendor");
        assertThat(environment.getProperty("springdoc.swagger-ui.urls[2].url"))
                .isEqualTo("/openapi/admin");
        assertThat(environment.getProperty("springdoc.swagger-ui.urls[3].url"))
                .isEqualTo("/openapi/catalog");
        assertThat(environment.getProperty("springdoc.swagger-ui.urls[4].url"))
                .isEqualTo("/openapi/operations");
    }
}
