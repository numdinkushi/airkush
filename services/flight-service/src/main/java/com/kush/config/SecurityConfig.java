package com.kush.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kush.security.JwtAuthenticationFilter;
import com.kush.security.SecurityProblemWriter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityProblemWriter problemWriter;

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/", "/api/airlines/**", "/api/flights/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/airlines/**", "/api/flights/**")
                            .hasAnyRole("ADMIN", "OWNER")
                    .requestMatchers(HttpMethod.PUT, "/api/airlines/**", "/api/flights/**")
                            .hasAnyRole("ADMIN", "OWNER")
                    .requestMatchers(HttpMethod.DELETE, "/api/airlines/**", "/api/flights/**")
                            .hasAnyRole("ADMIN", "OWNER")
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, ex) ->
                            problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Authentication required"))
                    .accessDeniedHandler((request, response, ex) ->
                            problemWriter.write(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action"))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
