package com.thartheeb.gateway;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
class ApiGatewayApplicationTests {
    @Autowired ApplicationContext context;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context)
            .apply(springSecurity()).build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void vendorActivationJwtCannotAuthorizeVendorApis() {
        client.mutateWith(mockJwt().jwt(jwt -> jwt.claim("scope", "vendor_activation")))
            .get().uri("/v1/vendor-applications/me")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void vendorScopeWithoutVendorAdminRoleCannotAuthorizeFleetApis() {
        client.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_vendor")))
            .get().uri("/v1/vendors/00000000-0000-0000-0000-000000000001/fleet/vehicles")
            .exchange()
            .expectStatus().isForbidden();
    }
}
