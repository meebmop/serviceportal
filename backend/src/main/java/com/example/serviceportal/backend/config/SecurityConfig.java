package com.example.serviceportal.backend.config;

import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final List<String> allowedOrigins;

    public SecurityConfig(@Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            Environment environment
    ) throws Exception {

        boolean devProfileActive = environment.acceptsProfiles(Profiles.of("dev"));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider)
                .securityContext(context -> context
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(authorize -> {
                    if (devProfileActive) {
                        authorize.requestMatchers("/h2-console/**").permitAll();
                    } else {
                        authorize.requestMatchers("/h2-console/**").denyAll();
                    }

                    authorize
                            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                            .requestMatchers(HttpMethod.GET, "/api/offers").permitAll()

                            .requestMatchers("/api/users/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/offers").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/offers/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/offers/**").hasRole("ADMIN")

                            .requestMatchers(HttpMethod.GET, "/api/requests/my").hasRole("USER")
                            .requestMatchers(HttpMethod.GET, "/api/requests").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("USER")
                            .requestMatchers(HttpMethod.PUT, "/api/requests/*/status").hasRole("ADMIN")

                            .anyRequest().authenticated();
                })
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(allowedOrigins);
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}