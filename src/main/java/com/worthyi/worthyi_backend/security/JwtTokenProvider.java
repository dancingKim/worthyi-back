//package com.worthyi.worthyi_backend.security;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.HttpServletRequest;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
////    @Value("${jwt.secret}")
//    private String secretKey;
//
//    // 토큰 유효시간 (예: 30분)
//    private final long tokenValidTime = 30 * 60 * 1000L;
//
//    private SecretKey key;
//
//    @PostConstruct
//    protected void init() {
//        // SecretKey 생성
//        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
//    }
//
//    // JWT 토큰 생성
//    public String createToken(String userPk) {
//        Claims claims = Jwts.claims().setSubject(userPk); // JWT payload에 저장되는 정보 단위
//        Date now = new Date();
//
//        return Jwts.builder()
//                .setClaims(claims) // 데이터
//                .setIssuedAt(now) // 토큰 발행일자
//                .setExpiration(new Date(now.getTime() + tokenValidTime)) // 만료일자
//                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘과 키
//                .compact();
//    }
//
//    // JWT 토큰에서 인증 정보 추출
//    public String getUserPk(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    // Request의 Header에서 토큰 값 가져오기 ("Authorization" : "Bearer 토큰")
//    public String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7); // "Bearer " 이후의 토큰 값
//        }
//        return null;
//    }
//
//    // 토큰의 유효성 + 만료일자 확인
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            // 로그를 남기거나 예외 처리 로직 추가 가능
//            return false;
//        }
//    }
//}
