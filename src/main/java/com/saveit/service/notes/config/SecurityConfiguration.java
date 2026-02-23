package com.saveit.service.notes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
public class SecurityConfiguration {

    @Profile("!prod")
    @Bean("utilityEndpointsWhitelist")
    public String[] utilityEndpointsWhitelistNonProd() {
        return new String[]{
                // -- actuator
                "/actuator/health", "/actuator/prometheus",
                // -- Swagger UI
                "/v3/api-docs/", "/swagger-ui.html", "/swagger-ui/"};
    }

    @Profile("prod")
    @Bean("utilityEndpointsWhitelist")
    public String[] utilityEndpointsWhitelistProd() {
        return new String[]{
                // -- actuator
                "/actuator/health", "/actuator/prometheus"};
    }

    @Bean
    public SecurityFilterChain generalFilterChainConfiguration(HttpSecurity http, String[] utilityEndpointsWhitelist) {
        return http.cors(this::setCorsConfig)
                .authorizeHttpRequests(configurer -> setAuthorizeRequestsConfig(configurer, utilityEndpointsWhitelist))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    private void setAuthorizeRequestsConfig(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizeHttpRequests,
            String[] utilityEndpointsWhitelist
    ) {
        authorizeHttpRequests
                .requestMatchers(utilityEndpointsWhitelist).permitAll()
                .requestMatchers("/note/**").permitAll()
                .anyRequest().denyAll();
    }

    private void setCorsConfig(CorsConfigurer<HttpSecurity> corsConfigurer) {
        final CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
        configuration.setAllowedMethods(List.of(GET.name(),
                HEAD.name(),
                POST.name(),
                PUT.name(),
                DELETE.name(),
                OPTIONS.name()));
        corsConfigurer.configurationSource(request -> configuration);
    }
}
