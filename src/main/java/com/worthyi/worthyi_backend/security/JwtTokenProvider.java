package com.worthyi.worthyi_backend.security;

import com.worthyi.worthyi_backend.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class JwtTokenProvider {

    private final String secretKey;
    private SecretKey key;

    private final long tokenValidTime = 30 * 60 * 1000L; // 토큰 유효 시간: 30분

    @Getter
    private final long refreshTokenValidTime = 30 * 60 * 1000L;

    private final StringRedisTemplate redisTemplate;

    public JwtTokenProvider(String secretKey, StringRedisTemplate redisTemplate) {
        this.secretKey = secretKey;
        this.redisTemplate = redisTemplate;
        init(); // key 초기화
    }

    private void init() {
        // SecretKey 초기화
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        log.info("SecretKey initialized.");
    }

    // JWT 토큰 생성 메소드
    public String createToken(Authentication authentication) {
        log.info("=== Creating JWT Token ===");
        log.debug("Creating token for authentication: Principal={}", authentication.getName());

        return createToken(authentication, authentication.getAuthorities());
    }

    public String createToken(Authentication authentication, Collection<? extends GrantedAuthority> roles) {
        log.info("=== Creating JWT Token with Roles ===");
        
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        log.debug("Principal details: userId={}, email={}", 
            principalDetails.getUser().getUserId(), 
            principalDetails.getUsername());

        String email = authentication.getName();
        String authorities = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        log.debug("Authorities to be included in token: {}", authorities);

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", authorities);
        claims.put("userId", principalDetails.getUser().getUserId());
        
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime expirationTime = now.atZone(ZoneId.systemDefault())
                                          .withZoneSameInstant(ZoneId.of("UTC"))
                                          .plus(tokenValidTime / (60 * 1000), ChronoUnit.MINUTES);
        Date expirationDate = Date.from(expirationTime.toInstant());

        log.debug("Token creation time (UTC): {}", now);
        log.debug("Token expiration time (UTC): {}", expirationDate);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("Token created successfully");
        log.debug("Token: {}", token);
        return token;
    }

    public String createRefreshToken(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        LocalDateTime now = LocalDateTime.now();
        String email = authentication.getName();
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", principalDetails.getUser().getUserId());

        ZonedDateTime expirationTime = now.atZone(ZoneId.systemDefault())
                                          .withZoneSameInstant(ZoneId.of("UTC"))
                                          .plus(refreshTokenValidTime / (60 * 1000), ChronoUnit.MINUTES);
        Date expirationDate = Date.from(expirationTime.toInstant());

        log.debug("Refresh token creation time (UTC): {}", now);
        log.debug("Refresh token expiration time (UTC): {}", expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // public String createRefreshToken(String email) {
    //     Date now = new Date();

    //     Claims claims = Jwts.claims().setSubject(email);

    //     return Jwts.builder()
    //             .setClaims(claims)
    //             .setIssuedAt(now)
    //             .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
    //             .signWith(key, SignatureAlgorithm.HS256)
    //             .compact();
    // }

   public Authentication getAuthentication(String token) {
        log.info("=== Getting Authentication from Token ===");
        
        Claims claims = parseClaims(token);
        log.debug("Claims extracted from token: subject={}", claims.getSubject());

        List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
        log.debug("Authorities parsed from token: {}", authorities);

        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        log.debug("User details from token: userId={}, email={}", userId, email);

        User user = User.builder().userId(userId).build();
        PrincipalDetails principal = new PrincipalDetails(user, Map.of(
                "email", email,
                "userId", userId
        ), "email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        log.info("Authentication object created successfully");
        return authentication;
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime expirationZoned = ZonedDateTime.ofInstant(expiration.toInstant(), ZoneId.of("UTC"));

            log.debug("Current time (UTC): {}", now);
            log.debug("Token expiration time (UTC): {}", expirationZoned);

            return expirationZoned.isBefore(now);
        } catch (Exception e) {
            return true;
        }
    }

    // Claims에서 권한 정보 추출
    private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        log.debug("Extracting authorities from claims.");

        String roles = claims.get("roles", String.class);
        if (roles == null || roles.isEmpty()) {
            log.warn("No roles found in token claims.");
            return List.of();
        }

        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        log.debug("Authorities extracted: {}", authorities);
        return authorities;
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // JWT 토큰에서 Claims 추출
    private Claims parseClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Claims parsed successfully.");
            return claims;
        } catch (JwtException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    // Request의 Header에서 토큰 값 가져오기 ("Authorization" : "Bearer [토큰]")
    public String resolveToken(HttpServletRequest request) {
        log.debug("Resolving token from request headers");
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Bearer token found and resolved");
            return token;
        }
        log.debug("No bearer token found in request");
        return null;
    }

    // 토큰의 유효성 및 만료일자 확인
    public boolean validateToken(String token) {
        log.info("=== Validating Token ===");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.info("Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("Invalid signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
        }
        return false;
    }

    public long getExpiration(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime();
    }
}
