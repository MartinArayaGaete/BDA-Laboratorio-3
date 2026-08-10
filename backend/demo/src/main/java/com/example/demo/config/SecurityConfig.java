package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // PÚBLICO
                        // ============================================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                        // ============================================
                        // ARQUERO - MongoDB
                        // ============================================
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos/*/podio").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos/*/climas").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos/*/rondas/*/posiciones").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos/*/rondas/*/arqueros/*/posicion").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/rondas/torneo/*").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/arqueros/*/historial").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/arqueros/*/estadisticas").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/participaciones/usuario/*").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.POST, "/api/mongo/participaciones").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.DELETE, "/api/mongo/participaciones/*/*").hasAnyRole("ADMIN", "ARQUERO")

                        // ============================================
                        // ARQUERO - SQL (lectura)
                        // ============================================
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/categorias-diana", "/api/categorias-diana/**").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/sectores-ambientales", "/api/sectores-ambientales/**").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/categorias-ambientales", "/api/categorias-ambientales/**").hasAnyRole("ADMIN", "ARQUERO")
                        .requestMatchers(HttpMethod.GET, "/api/mapas/**").hasAnyRole("ADMIN", "ARQUERO")

                        // ============================================
                        // ADMIN - SQL
                        // ============================================
                        .requestMatchers("/api/logs", "/api/logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/categorias", "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers("/api/categorias-diana", "/api/categorias-diana/**").hasRole("ADMIN")
                        .requestMatchers("/api/sectores-ambientales", "/api/sectores-ambientales/**").hasRole("ADMIN")
                        .requestMatchers("/api/categorias-ambientales", "/api/categorias-ambientales/**").hasRole("ADMIN")
                        .requestMatchers("/api/estadisticas/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN - MongoDB Torneos
                        // ============================================
                        .requestMatchers(HttpMethod.POST, "/api/mongo/torneos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mongo/torneos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mongo/torneos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mongo/torneos/*/iniciar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mongo/torneos/*/finalizar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mongo/torneos/*/siguiente-ronda").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/mongo/torneos/*/rondas/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/torneos/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN - MongoDB Rondas
                        // ============================================
                        .requestMatchers(HttpMethod.POST, "/api/mongo/rondas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mongo/rondas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mongo/rondas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/rondas", "/api/mongo/rondas/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN - MongoDB Puntuaciones
                        // ============================================
                        .requestMatchers(HttpMethod.POST, "/api/mongo/puntuaciones", "/api/mongo/puntuaciones/registrar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mongo/puntuaciones/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/puntuaciones", "/api/mongo/puntuaciones/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN - MongoDB Participaciones
                        // ============================================
                        .requestMatchers(HttpMethod.GET, "/api/mongo/participaciones", "/api/mongo/participaciones/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN - MongoDB Arqueros, Pipeline, Ranking
                        // ============================================
                        .requestMatchers(HttpMethod.GET, "/api/mongo/arqueros", "/api/mongo/arqueros/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/pipeline", "/api/mongo/pipeline/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mongo/ranking", "/api/mongo/ranking/**").hasRole("ADMIN")

                        // ============================================
                        // BLOQUEAR TODO LO DEMÁS
                        // ============================================
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://172.*.*.*:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}