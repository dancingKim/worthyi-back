package com.worthyi.worthyi_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;

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
        log.debug("Creating JWT token for authentication: {}", authentication);

        String email = authentication.getName(); // Principal의 이름을 이메일로 가정
        // 사용자 권한 정보를 문자열로 변환
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return createToken(email, authentication.getAuthorities());
    }

    public String createToken(String email, Collection<? extends GrantedAuthority> roles) {

        String authorities = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // JWT Claims 설정
        Claims claims = Jwts.claims().setSubject(email); // 이메일을 subject로 사용
        claims.put("roles", authorities); // 권한 정보를 추가
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims) // 사용자 정보 설정
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘과 키 설정
                .compact();

        return token;
    }

    public String createRefreshToken(Authentication authentication) {
        String email = authentication.getName();
        return createRefreshToken(email);
    }

    public String createRefreshToken(String email) {
        Date now = new Date();

        Claims claims = Jwts.claims().setSubject(email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        log.debug("Getting authentication from token.");

        Claims claims = parseClaims(token);
        log.debug("Parsed claims: {}", claims);

        List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
        log.debug("Extracted authorities: {}", authorities);

        // UserDetails 객체 생성 (비밀번호는 빈 문자열로 설정)
        User principal = new User(claims.getSubject(), "", authorities);

        // 인증 정보 반환
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        log.debug("Authentication object created: {}", authentication);

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

            return expiration.before(new Date());
        } catch (Exception e){
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
        log.debug("Resolving token from request header.");

        String bearerToken = request.getHeader("Authorization");
        // "Bearer "로 시작하는지 확인
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7); // "Bearer " 이후의 토큰 값 반환
            log.debug("Token resolved: {}", token);
            return token;
        }
        log.warn("Authorization header is missing or does not start with 'Bearer '.");
        return null;
    }

    // 토큰의 유효성 및 만료일자 확인
    public boolean validateToken(String token) {
        log.debug("Validating token.");

        try {
            // 토큰 파싱 및 검증
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token is valid.");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument token: {}", e.getMessage());
        }
        return false;
    }

    public long getExpiration(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime();
    }
}
