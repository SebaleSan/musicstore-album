package com.duoc.album.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.album.controller.StatusController;

@WebMvcTest(StatusController.class)
@Import({SecurityConfig.class, SecurityRulesTest.TestUsers.class})
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statusIsPublic() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void anonymousReadIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/items"));
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items").header(HttpHeaders.AUTHORIZATION, basic("client", "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userCanRead() throws Exception {
        mockMvc.perform(get("/api/items").header(HttpHeaders.AUTHORIZATION, basic("client", "secret")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotWrite() throws Exception {
        mockMvc.perform(post("/api/items").header(HttpHeaders.AUTHORIZATION, basic("client", "secret")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/items"));
    }

    @Test
    void adminCanWrite() throws Exception {
        mockMvc.perform(post("/api/items").header(HttpHeaders.AUTHORIZATION, basic("admin", "secret")))
                .andExpect(status().isNotFound());
    }

    private static String basic(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @TestConfiguration
    static class TestUsers {

        @Bean
        AuthenticationProvider authenticationProvider() {
            return new DaoAuthenticationProvider(new InMemoryUserDetailsManager(
                    User.withUsername("client").password("{noop}secret").roles("USER").build(),
                    User.withUsername("admin").password("{noop}secret").roles("ADMIN").build()));
        }
    }
}
