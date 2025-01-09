package com.worthyi.worthyi_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.AvatarInVillage;
import com.worthyi.worthyi_backend.model.entity.AvatarInVillageId;
import com.worthyi.worthyi_backend.model.entity.PlaceInstance;
import com.worthyi.worthyi_backend.model.entity.Role;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.model.entity.UserRole;
import com.worthyi.worthyi_backend.model.entity.VillageInstance;
import com.worthyi.worthyi_backend.repository.AvatarInVillageRepository;
import com.worthyi.worthyi_backend.repository.AvatarRepository;
import com.worthyi.worthyi_backend.repository.PlaceInstanceRepository;
import com.worthyi.worthyi_backend.repository.PlaceTemplateRepository;
import com.worthyi.worthyi_backend.repository.RoleRepository;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.repository.UserRoleRepository;
import com.worthyi.worthyi_backend.repository.VillageInstanceRepository;
import com.worthyi.worthyi_backend.repository.VillageTemplateRepository;
import com.worthyi.worthyi_backend.security.OAuth2UserInfo;
import com.worthyi.worthyi_backend.security.OidcPrincipalDetails;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
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

        // 먼저 OidcUserService가 제공하는 기본 OidcUser 로드
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("OIDC provider: {}", registrationId);

        // Apple의 경우, id_token을 디코딩해서 필요한 값들을 추출할 수 있음
        String idToken = userRequest.getIdToken().getTokenValue();
        log.debug("Received idToken: {}", idToken);

        // id_token Payload 디코딩
        Map<String, Object> attributes = decodeJwtTokenPayload(idToken);
        log.debug("Decoded attributes: {}", attributes);

        // OIDC 표준 claims도 합쳐줌
        attributes.putAll(oidcUser.getAttributes());

        // provider, sub 등 정보 추출
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
        log.debug("OIDC UserInfo created: {}", oAuth2UserInfo);

        // DB에 기존 사용자 존재 여부 확인
        Optional<User> userOptional = userRepository.findByProviderAndSub(
                oAuth2UserInfo.getProvider(),
                oAuth2UserInfo.getSub()
        );

        User user = userOptional.orElseGet(() -> {
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

        // 리턴할 attributes 에 사용자 데이터 추가
        attributes.put("userId", user.getUserId().toString());
        attributes.put("provider", user.getProvider());
        attributes.put("sub", user.getSub());
        attributes.put("authorities", user.getAuthorities());

        log.info("=== OIDC User Loading End ===");

        // 우리가 사용하는 PrincipalDetails로 반환 (OIDC 표준 OidcUser 대신)
        return new OidcPrincipalDetails(oidcUser, attributes, "userId");
    }

    /**
     * Apple 등에서 받은 id_token의 Payload 부분(Base64) 디코딩
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