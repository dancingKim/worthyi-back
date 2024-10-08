package com.worthyi.worthyi_backend.service;

import java.util.Optional;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.security.OAuth2UserInfo;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("OAuth2UserRequest received: {}", userRequest);
        // 기본 구현체를 통해 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User loaded: {}", oAuth2User.getAttributes());

        // OAuth2 공급자 ID (예: google, kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 registrationId: {}", registrationId);

        // 사용자 정보 맵 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("OAuth2 attributes: {}", attributes);

        // 사용자 정보를 OAuth2UserInfo 객체로 변환
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
        log.info("OAuth2UserInfo: {}", oAuth2UserInfo);

        // 사용자 이메일로 데이터베이스에서 사용자 조회
        Optional<User> userOptional = userRepository.findByEid(oAuth2UserInfo.email());
        User user = userOptional.orElseGet(() -> {
            log.info("User not found, creating new user with email: {}", oAuth2UserInfo.email());
            // 사용자 없을 시 신규 생성 후 저장
            User newUser = oAuth2UserInfo.toEntity();
            User savedUser = userRepository.save(newUser);

            return savedUser;
        });
        log.info("User loaded: {}", user);
        // PrincipalDetails 객체 반환 (OAuth2User 구현체)
        return new PrincipalDetails(user, attributes, "email");
    }
}