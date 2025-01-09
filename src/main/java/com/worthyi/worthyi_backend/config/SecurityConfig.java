package com.worthyi.worthyi_backend.config;

import com.worthyi.worthyi_backend.security.CustomAuthenticationEntryPoint;
import com.worthyi.worthyi_backend.security.CustomRequestEntityConverter;
import com.worthyi.worthyi_backend.security.JwtAuthenticationFilter;
import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import com.worthyi.worthyi_backend.security.OAuth2AuthenticationSuccessHandler;
import com.worthyi.worthyi_backend.security.RedirectUrlCookieFilter;
import com.worthyi.worthyi_backend.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomOAuth2UserService oAuth2UserService;
    private final RedirectUrlCookieFilter redirectUrlCookieFilter;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(CustomRequestEntityConverter customRequestEntityConverter) {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        accessTokenResponseClient.setRequestEntityConverter(customRequestEntityConverter);
        log.debug("accessTokenResponseClient configured with CustomRequestEntityConverter: {}", customRequestEntityConverter);
        return accessTokenResponseClient;
    }

    @Bean
    public CustomRequestEntityConverter customRequestEntityConverter() {
        CustomRequestEntityConverter converter = new CustomRequestEntityConverter();
        log.debug("CustomRequestEntityConverter bean created: {}", converter);
        return converter;
    }

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
                                "/",
                                "/login",
                                "/auth/**",
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
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((_, response, exception) -> {
                            if (exception instanceof OAuth2AuthenticationException) {
                                OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
                                log.error("OAuth2 Authentication Exception: {}", oauth2Exception.getError().getDescription());
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + oauth2Exception.getError().getDescription());
                            } else {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                            }
                        })
                )
                .exceptionHandling(ex -> 
                    ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                    .accessDeniedHandler((_, response, _) -> {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                    })
                );

        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .addFilterBefore(redirectUrlCookieFilter, OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate ) {
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
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // React Native 앱에서의 요청을 허용
        configuration.addAllowedOrigin("http://localhost:3000"); // 웹용
        configuration.addAllowedOrigin("http://192.168.0.6:8081"); // Metro Bundler를 사용하는 React Native 앱
        configuration.addAllowedOrigin("http://10.0.2.2:8081"); // Android 에뮬레이터용
        configuration.addAllowedOrigin("https://appleid.apple.com");
        configuration.addAllowedOriginPattern("*"); // 모든 요청 허용 (배포 환경에서는 주의)

        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
