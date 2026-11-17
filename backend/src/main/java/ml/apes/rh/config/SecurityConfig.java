package ml.apes.rh.config;

import java.util.List;
import ml.apes.rh.journal.ActivityLogRepository;
import ml.apes.rh.security.ActivityLogFilter;
import ml.apes.rh.security.AppUserDetailsService;
import ml.apes.rh.security.JwtAuthFilter;
import ml.apes.rh.security.JwtService;
import ml.apes.rh.security.RestAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity // active @PreAuthorize sur chaque méthode des contrôleurs
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtService jwtService,
        AppUserDetailsService userDetailsService,
        ActivityLogRepository logRepository,
        RestAuthEntryPoint entryPoint,
        @Value("${app.auth-cookie-secure:false}") boolean cookieSecure
    ) throws Exception {
        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtService, userDetailsService);
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");
        csrfRepository.setCookieCustomizer(cookie ->
            cookie.path("/").sameSite("Lax").secure(cookieSecure)
        );
        http.csrf(c ->
            c
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                // Logout only expires the authentication cookie; a cross-site logout cannot change server data.
                .ignoringRequestMatchers("/api/auth/logout")
        )
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a ->
                a
                    .requestMatchers(
                        "/api/auth/csrf",
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/auth/mot-de-passe-oublie",
                        "/api/auth/reinitialiser-mot-de-passe"
                    )
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new ActivityLogFilter(logRepository), JwtAuthFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
        @Value("${app.cors-origins}") List<String> origins
    ) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origins);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Content-Disposition"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", c);
        return source;
    }
}
