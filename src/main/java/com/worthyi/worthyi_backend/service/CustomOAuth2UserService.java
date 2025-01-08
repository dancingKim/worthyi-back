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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.debug("OAuth2User loaded from provider: attributes={}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("OAuth2 provider: {}", registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.debug("OAuth2 attributes received: {}", attributes);

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
        log.debug("OAuth2UserInfo created: {}", oAuth2UserInfo);
        
        Optional<User> userOptional = userRepository.findByProviderAndSub(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getSub());
        // Optional<User> userOptional = userRepository.findByProviderUserIdWithRoles(oAuth2UserInfo.getProviderUserId());
        
        log.debug("Existing user check: {}", userOptional.isPresent() ? "found" : "not found");

        User user = userOptional.orElseGet(() -> {

            log.info("Creating new user for providerUserId: {}", oAuth2UserInfo.getSub());
            // 사용자 없을 시 신규 생성 후 저장
            User newUser = oAuth2UserInfo.toEntity();
            newUser.setUserRoles(new ArrayList<>());  // 빈 리스트로 초기화
            User savedUser = userRepository.save(newUser);
            log.info("New user created: {}", savedUser);

            // 역할 설정
            Role userRole = roleRepository.findByAuthorityName("ROLE_USER")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .authorityName("ROLE_USER")
                                .build();
                        return roleRepository.save(role);
                    });

            // UserRole 엔티티 생성 및 저장
            UserRole userRoleEntity = UserRole.builder()
                    .user(savedUser)
                    .role(userRole)
                    .build();
            userRoleRepository.save(userRoleEntity);
            savedUser.getUserRoles().add(userRoleEntity);  // 생성된 UserRole 추가

            // VillageInstance 생성
            VillageInstance villageInstance = VillageInstance.builder()
                    .user(savedUser)
                    .villageTemplate(villageTemplateRepository.findById(1L).orElseThrow())
                    .name(savedUser.getUserId().toString() + "의 마을")
                    .description("새로 만들어진 마을입니다.")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            villageInstanceRepository.save(villageInstance);

            // Avatar 생성
            Avatar avatar = Avatar.builder()
                    .user(savedUser)
                    .name(savedUser.getSub() + "의 아바타")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            avatarRepository.save(avatar);

            // PlaceInstance 생성
            PlaceInstance placeInstance = PlaceInstance.builder()
                    .placeTemplate(placeTemplateRepository.findById(1L).orElseThrow())
                    .villageInstance(villageInstance)
                    .name("기본 장소 이름")
                    .description("새로 만들어진 장소입니다.")
                    .build();
            placeInstanceRepository.save(placeInstance);

            // 예) CustomOAuth2UserService.loadUser() 안에 추가
            AvatarInVillageId avatarInVillageId = new AvatarInVillageId(
                    avatar.getAvatarId(),
                    villageInstance.getVillageId());

            AvatarInVillage avatarInVillage = AvatarInVillage.builder()
                    .id(avatarInVillageId) // 복합 키 설정
                    .avatar(avatar) // @MapsId("avatarId") 부분
                    .villageInstance(villageInstance) // @MapsId("villageId") 부분
                    .build();

            // 이제 복합 키가 직접 세팅됐으므로 충돌 없이 저장 가능
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
        return new PrincipalDetails(user, attributes, "userId");
    }
}
