package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void logout(String accessToken, String email){
        redisTemplate.delete("refresh:" + email);

        long expiration = jwtTokenProvider.getExpiration(accessToken) - System.currentTimeMillis();
        redisTemplate.opsForValue().set("blacklist:"+accessToken, "logout",expiration, TimeUnit.MICROSECONDS);
    }
}
