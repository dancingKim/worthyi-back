package com.worthyi.worthyi_backend.config;

import com.worthyi.worthyi_backend.security.JwtAuthenticationFilter;
import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import com.worthyi.worthyi_backend.security.OAuth2AuthenticationSuccessHandler;
import com.worthyi.worthyi_backend.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 및 소스 지정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // HTTP 기본 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                // CSRF 보안 비활성화 (JWT 사용 시 필요 없음)
                .csrf(csrf -> csrf.disable())
                // 세션 사용하지 않음 (JWT로 인증)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청에 대한 권한 설정
                .authorizeRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers(
                                "/", "/login", "/oauth2/**",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        // 그 외의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // OAuth2 로그인 성공 후 처리 설정
                        .userInfoEndpoint(c -> c.userService(oAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler()))
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // OAuth2 인증 성공 시 처리 핸들러 빈 등록
    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(jwtTokenProvider);
    }

    // CORS 설정 소스 빈 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 도메인 허용 (실제 서비스 시에는 특정 도메인만 허용)
        configuration.addAllowedOrigin("*");
        // 모든 HTTP 메소드 허용
        configuration.addAllowedMethod("*");
        // 모든 헤더 허용
        configuration.addAllowedHeader("*");
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        // Pre-flight 요청 캐시 시간 설정
        configuration.setMaxAge(3600L);
        // 모든 경로에 대해 위의 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}