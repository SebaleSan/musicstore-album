package com.duoc.album.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.album.dto.AlbumRequest;
import com.duoc.album.error.ResourceNotFoundException;
import com.duoc.album.model.Album;
import com.duoc.album.security.SecurityConfig;
import com.duoc.album.service.AlbumService;

@WebMvcTest(AlbumController.class)
@Import({SecurityConfig.class, AlbumControllerTest.TestUsers.class})
class AlbumControllerTest {

    private static final String VALID_ALBUM = """
            {"artist":"Boris","title":"Pink","year":2005,"genre":"Stoner Rock","price":31990,"stock":69}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @Test
    void userCanListAlbums() throws Exception {
        given(albumService.findAll()).willReturn(List.of(sampleAlbum()));

        mockMvc.perform(get("/api/albums").header(HttpHeaders.AUTHORIZATION, basic("client")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAlbum").value(1))
                .andExpect(jsonPath("$[0].title").value("Pink"));
    }

    @Test
    void anonymousCanListAlbums() throws Exception {
        given(albumService.findAll()).willReturn(List.of(sampleAlbum()));

        mockMvc.perform(get("/api/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pink"));
    }

    @Test
    void anonymousCannotCreateAlbum() throws Exception {
        mockMvc.perform(post("/api/albums").contentType(MediaType.APPLICATION_JSON).content(VALID_ALBUM))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas o ausentes"));
    }

    @Test
    void userCannotCreateAlbum() throws Exception {
        mockMvc.perform(post("/api/albums").header(HttpHeaders.AUTHORIZATION, basic("client"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_ALBUM))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    void userCannotDeleteAlbum() throws Exception {
        mockMvc.perform(delete("/api/albums/1").header(HttpHeaders.AUTHORIZATION, basic("client")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAlbum() throws Exception {
        given(albumService.create(any(AlbumRequest.class))).willReturn(sampleAlbum());

        mockMvc.perform(post("/api/albums").header(HttpHeaders.AUTHORIZATION, basic("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_ALBUM))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlbum").value(1));
    }

    @Test
    void createRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/albums").header(HttpHeaders.AUTHORIZATION, basic("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"artist\":\"Boris\",\"title\":\"Pink\",\"price\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solicitud inválida"))
                .andExpect(jsonPath("$.fields.genre").value("El género es obligatorio"))
                .andExpect(jsonPath("$.fields.stock").value("El stock es obligatorio"));
    }

    @Test
    void unknownAlbumReturnsNotFound() throws Exception {
        given(albumService.findById(99L)).willThrow(new ResourceNotFoundException("Álbum no encontrado con id: 99"));

        mockMvc.perform(get("/api/albums/99").header(HttpHeaders.AUTHORIZATION, basic("client")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Álbum no encontrado con id: 99"));
    }

    private static Album sampleAlbum() {
        return Album.builder()
                .idAlbum(1L)
                .artist("Boris")
                .title("Pink")
                .year(2005)
                .genre("Stoner Rock")
                .price(31990L)
                .stock(69)
                .build();
    }

    private static String basic(String username) {
        String credentials = username + ":secret";
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
