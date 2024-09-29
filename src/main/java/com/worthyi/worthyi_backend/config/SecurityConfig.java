//package com.worthyi.worthyi_backend.config;
//
//import com.worthyi.worthyi_backend.security.JwtAuthenticationFilter;
//import com.worthyi.worthyi_backend.security.JwtTokenProvider;
//import com.worthyi.worthyi_backend.security.OAuth2AuthenticationSuccessHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import io.jsonwebtoken.*;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
////    private final JwtTokenProvider jwtTokenProvider;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .httpBasic(httpBasic -> httpBasic.disable())
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeRequests(auth -> auth
//                        .requestMatchers("/", "/login", "/oauth2/**", "/css/**", "/js/**", "/images/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .successHandler(oAuth2AuthenticationSuccessHandler())
//                )
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
//                        JwtAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
//        return new OAuth2AuthenticationSuccessHandler(jwtTokenProvider);
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.addAllowedOrigin("*"); // 실제 서비스에서는 허용할 도메인을 명시적으로 설정
//        configuration.addAllowedMethod("*");
//        configuration.addAllowedHeader("*");
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}
