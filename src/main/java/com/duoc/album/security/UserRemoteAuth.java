package com.duoc.album.security;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserRemoteAuth implements AuthenticationProvider {

    private final RestClient client;

    public UserRemoteAuth(@Value("${app.users.url}") String usersUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder()
                .baseUrl(usersUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            RemoteProfile profile = client.get()
                    .uri("/api/auth/profile")
                    .headers(headers -> headers.setBasicAuth(username, password))
                    .retrieve()
                    .body(RemoteProfile.class);
            if (profile == null || profile.role() == null || !username.equals(profile.user())) {
                throw new BadCredentialsException("Invalid user profile");
            }
            return UsernamePasswordAuthenticationToken.authenticated(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_" + profile.role())));
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            throw new BadCredentialsException("Invalid credentials");
        } catch (RestClientException ex) {
            throw new AuthenticationServiceException("User service unavailable", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
