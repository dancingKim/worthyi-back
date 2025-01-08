package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.model.entity.Role;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.model.entity.UserRole;
import com.worthyi.worthyi_backend.repository.RoleRepository;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.repository.UserRoleRepository;
import com.worthyi.worthyi_backend.security.OAuth2UserInfo;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.model.entity.VillageInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.PlaceInstance;
import com.worthyi.worthyi_backend.repository.VillageTemplateRepository;
import com.worthyi.worthyi_backend.repository.PlaceTemplateRepository;
import com.worthyi.worthyi_backend.repository.VillageInstanceRepository;
import com.worthyi.worthyi_backend.repository.AvatarRepository;
import com.worthyi.worthyi_backend.repository.PlaceInstanceRepository;
import com.worthyi.worthyi_backend.model.entity.AvatarInVillage;
import com.worthyi.worthyi_backend.model.entity.AvatarInVillageId;
import com.worthyi.worthyi_backend.repository.AvatarInVillageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OAuth2 User Loading Start ===");
        OAuth2UserInfo oAuth2UserInfo;
        Map<String, Object> attributes = new HashMap<>();

        if (userRequest.getClientRegistration().getRegistrationId().equals("apple")) {
            log.debug("Processing Apple OAuth2 login");
            String idToken = userRequest.getAdditionalParameters().get("id_token").toString();
            log.debug("Received idToken: {}", idToken);
            attributes.putAll(decodeJwtTokenPayload(idToken));
            log.debug("Decoded attributes: {}", attributes);
            oAuth2UserInfo = OAuth2UserInfo.of("apple", attributes);
            log.debug("OAuth2UserInfo created: {}", oAuth2UserInfo);
        } else {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.debug("OAuth2User loaded from provider: attributes={}", oAuth2User.getAttributes());

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.debug("OAuth2 provider: {}", registrationId);

            attributes.putAll(oAuth2User.getAttributes());
            log.debug("OAuth2 attributes received: {}", attributes);

            oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
            log.debug("OAuth2UserInfo created: {}", oAuth2UserInfo);
        }

        Optional<User> userOptional = userRepository.findByProviderAndSub(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getSub());
        log.debug("Existing user check: {}", userOptional.isPresent() ? "found" : "not found");

        User user = userOptional.orElseGet(() -> {
            log.debug("Creating new user for provider: {}", oAuth2UserInfo.getProvider());
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

            VillageInstance villageInstance = VillageInstance.builder()
                    .user(savedUser)
                    .villageTemplate(villageTemplateRepository.findById(1L).orElseThrow())
                    .name(savedUser.getUserId().toString() + "의 마을")
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

            AvatarInVillageId avatarInVillageId = new AvatarInVillageId(
                    avatar.getAvatarId(),
                    villageInstance.getVillageId());

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

        attributes.put("userId", user.getUserId().toString());
        attributes.put("provider", user.getProvider());
        attributes.put("sub", user.getSub());
        attributes.put("authorities", user.getAuthorities());
        log.info("=== OAuth2 User Loading End ===");
        return new PrincipalDetails(attributes, "userId");
    }

    public Map<String, Object> decodeJwtTokenPayload(String jwtToken) {
        log.debug("Decoding JWT token payload");
        Map<String, Object> jwtClaims = new HashMap<>();
        try {
            String[] parts = jwtToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            byte[] decodedBytes = decoder.decode(parts[1].getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.readValue(decodedString, new TypeReference<Map<String, Object>>() {});
            jwtClaims.putAll(map);
            log.debug("JWT claims decoded: {}", jwtClaims);

        } catch (JsonProcessingException e) {
            log.error("Error decoding JWT token: {}", e.getMessage());
        }
        return jwtClaims;
    }
}
