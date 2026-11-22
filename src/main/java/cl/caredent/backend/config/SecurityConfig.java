package cl.caredent.backend.config;

import cl.caredent.backend.repository.UsuarioRepository;
import cl.caredent.backend.security.JwtAuthFilter;
import cl.caredent.backend.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, UsuarioRepository usuarios) throws Exception {
        // El filtro NO es un @Component a proposito: asi solo corre dentro de la cadena de Spring Security.
        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtService, usuarios);

        http
            .csrf(csrf -> csrf.disable()) // API sin cookies de sesion: la autenticacion va en el header Authorization
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/salud", "/error").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401, "No autenticado", "Debes iniciar sesión o tu sesión expiró.", req.getRequestURI()))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403, "Prohibido", "No tienes permisos para realizar esta acción.", req.getRequestURI())))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${caredent.cors.allowed-origins}") String origenes) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static void escribir(jakarta.servlet.http.HttpServletResponse res, int status, String error, String mensaje, String path)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + mensaje + "\",\"path\":\""
                + path.replace("\"", "") + "\"}";
        res.getWriter().write(json);
    }
}
