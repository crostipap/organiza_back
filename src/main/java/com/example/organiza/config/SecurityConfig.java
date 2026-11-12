package com.example.organiza.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig{

    @Autowired
    private JwtConverter jwtConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserSyncFilter userSyncFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize)-> authorize
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        (oauth2)-> oauth2.jwt(
                                jwt-> jwt.jwtAuthenticationConverter(jwtConverter)
                        ))
                .sessionManagement(
                        session-> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
                )
                .addFilterAfter(userSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<UserSyncFilter> userSyncFilterRegistration(UserSyncFilter userSyncFilter) {
        FilterRegistrationBean<UserSyncFilter> registration = new FilterRegistrationBean<>(userSyncFilter);
        registration.setEnabled(false);
        return registration;
    }
}