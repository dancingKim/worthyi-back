package com.worthyi.worthyi_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.model.entity.*;
import com.worthyi.worthyi_backend.repository.*;
import com.worthyi.worthyi_backend.security.OAuth2UserInfo;
import com.worthyi.worthyi_backend.security.OidcPrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VillageTemplateRepository villageTemplateRepository;
    private final PlaceTemplateRepository placeTemplateRepository;
    private final VillageInstanceRepository villageInstanceRepository;
    private final AvatarRepository avatarRepository;
    private final PlaceInstanceRepository placeInstanceRepository;
    private final AvatarInVillageRepository avatarInVillageRepository;

    @Transactional
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OIDC User Loading Start (e.g. Apple) ===");
        log.debug("OidcUserRequest: {}", userRequest);

        // 1) OidcUserService의 기본 로직으로 OidcUser 가져옴
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("OIDC provider: {}", registrationId);

        // 2) Apple의 id_token
        String idToken = userRequest.getIdToken().getTokenValue();
        log.debug("Received idToken: {}", idToken);

        // 2-1) 직접 디코딩한 claims
        Map<String, Object> attributes = decodeJwtTokenPayload(idToken);
        log.debug("Decoded attributes: {}", attributes);

        // 2-2) OIDC 표준 Claims 합치기
        attributes.putAll(oidcUser.getAttributes());

        // 3) provider, sub 추출
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
        log.debug("OIDC UserInfo created: {}", oAuth2UserInfo);

        // 4) DB 조회
        Optional<User> userOptional = userRepository.findByProviderAndSub(
                oAuth2UserInfo.getProvider(),
                oAuth2UserInfo.getSub()
        );

        User user = userOptional.orElseGet(() -> {
            // 신규 유저 생성
            log.debug("Creating new user for OIDC provider: {}", oAuth2UserInfo.getProvider());
            User newUser = oAuth2UserInfo.toEntity();
            newUser.setUserRoles(new ArrayList<>());
            User savedUser = userRepository.save(newUser);
            log.info("New user created: {}", savedUser);

            Role userRole = roleRepository.findByAuthorityName("ROLE_USER")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .authorityName("ROLE_USER")
                                .build();
                        return roleRepository.save(role);
                    });
            UserRole userRoleEntity = UserRole.builder()
                    .user(savedUser)
                    .role(userRole)
                    .build();
            userRoleRepository.save(userRoleEntity);
            savedUser.getUserRoles().add(userRoleEntity);

            // 마을, 아바타, 장소 등 초기화
            VillageInstance villageInstance = VillageInstance.builder()
                    .user(savedUser)
                    .villageTemplate(villageTemplateRepository.findById(1L).orElseThrow())
                    .name(savedUser.getUserId() + "의 마을")
                    .description("새로 만들어진 마을입니다.")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            villageInstanceRepository.save(villageInstance);

            Avatar avatar = Avatar.builder()
                    .user(savedUser)
                    .name(savedUser.getSub() + "의 아바타")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            avatarRepository.save(avatar);

            PlaceInstance placeInstance = PlaceInstance.builder()
                    .placeTemplate(placeTemplateRepository.findById(1L).orElseThrow())
                    .villageInstance(villageInstance)
                    .name("기본 장소 이름")
                    .description("새로 만들어진 장소입니다.")
                    .build();
            placeInstanceRepository.save(placeInstance);

            AvatarInVillageId avatarInVillageId = new AvatarInVillageId(avatar.getAvatarId(), villageInstance.getVillageId());
            AvatarInVillage avatarInVillage = AvatarInVillage.builder()
                    .id(avatarInVillageId)
                    .avatar(avatar)
                    .villageInstance(villageInstance)
                    .build();

            avatarInVillageRepository.save(avatarInVillage);
            log.info("Created instances - Village: {}, Avatar: {}, Place: {}, AvatarInVillage: {}",
                    villageInstance, avatar, placeInstance, avatarInVillage);

            return savedUser;
        });

        // 5) 반환용 attributes (PrincipalDetails에 담길 정보)
        attributes.put("userId", user.getUserId().toString());
        attributes.put("provider", user.getProvider());
        attributes.put("sub", user.getSub());
        attributes.put("authorities", user.getAuthorities());

        log.info("=== OIDC User Loading End ===");

        // 6) OidcPrincipalDetails 리턴
        return new OidcPrincipalDetails(oidcUser, attributes, "userId");
    }

    /**
     * Apple id_token Payload (Base64) 디코딩
     */
    private Map<String, Object> decodeJwtTokenPayload(String jwtToken) {
        log.debug("Decoding JWT token payload");
        Map<String, Object> jwtClaims = new HashMap<>();
        try {
            String[] parts = jwtToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            byte[] decodedBytes = decoder.decode(parts[1].getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.readValue(decodedString, new TypeReference<>() {});
            jwtClaims.putAll(map);
            log.debug("JWT claims decoded: {}", jwtClaims);

        } catch (JsonProcessingException e) {
            log.error("Error decoding JWT token: {}", e.getMessage());
        }
        return jwtClaims;
    }
}