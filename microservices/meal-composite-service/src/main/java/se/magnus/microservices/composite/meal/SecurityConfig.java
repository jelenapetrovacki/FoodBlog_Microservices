package se.magnus.microservices.composite.meal;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.*;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers(POST, "/meal-composite/**").hasAuthority("SCOPE_meal:write")
                .pathMatchers(DELETE, "/meal-composite/**").hasAuthority("SCOPE_meal:write")
                .pathMatchers(GET, "/meal-composite/**").hasAuthority("SCOPE_meal:read")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}