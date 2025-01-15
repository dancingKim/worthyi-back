package com.worthyi.worthyi_backend.config;

import com.worthyi.worthyi_backend.security.CustomAuthenticationEntryPoint;
import com.worthyi.worthyi_backend.security.CustomRequestEntityConverter;
import com.worthyi.worthyi_backend.security.JwtAuthenticationFilter;
import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import com.worthyi.worthyi_backend.security.OAuth2AuthenticationSuccessHandler;
import com.worthyi.worthyi_backend.security.RedirectUrlCookieFilter;
import com.worthyi.worthyi_backend.security.OAuth2AuthenticationFailureHandler;
import com.worthyi.worthyi_backend.security.CustomAccessDeniedHandler;
import com.worthyi.worthyi_backend.service.CustomOAuth2UserService;
import com.worthyi.worthyi_backend.service.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomOAuth2UserService oAuth2UserService;
    private final CustomOidcUserService oidcUserService;
    private final RedirectUrlCookieFilter redirectUrlCookieFilter;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**",
                                "/auth/authorize/**",
                                "/auth/token/**",
                                "/",
                                "/login",
                                "/auth/**",
                                "/auth/token/refresh",
                                "/oauth2/**",
                                "/actuator/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .tokenEndpoint(token -> token
                                .accessTokenResponseClient(accessTokenResponseClient(customRequestEntityConverter()))
                        )
                        // userInfoEndpoint: 일반 OAuth2 vs. OIDC(Apple) 각각 서비스 등록
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)  // 일반 OAuth2(예: Google)
                                .oidcUserService(oidcUserService) // OIDC(예: Apple)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                                .accessDeniedHandler(customAccessDeniedHandler)
                );

        // JWT 필터
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 소셜 로그인 Redirect 용 쿠키 필터
        http.addFilterBefore(redirectUrlCookieFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Apple 인증 시 client_secret 생성을 위해 사용
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(
            CustomRequestEntityConverter customRequestEntityConverter) {

        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRequestEntityConverter(customRequestEntityConverter);
        return client;
    }

    @Bean
    public CustomRequestEntityConverter customRequestEntityConverter() {
        return new CustomRequestEntityConverter();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                          StringRedisTemplate redisTemplate) {
        return new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate);
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                                                           StringRedisTemplate redisTemplate) {
        return new OAuth2AuthenticationSuccessHandler(jwtTokenProvider, redisTemplate);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(StringRedisTemplate redisTemplate) {
        return new JwtTokenProvider(secretKey, redisTemplate);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 필요에 따라 허용 도메인 설정
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://192.168.0.6:8081",
                "http://10.0.2.2:8081",
                "https://appleid.apple.com"
        ));
        configuration.addAllowedOriginPattern("*");

        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}