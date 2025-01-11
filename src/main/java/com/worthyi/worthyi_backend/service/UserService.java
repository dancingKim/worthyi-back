package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.UserDto;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.model.entity.UserRole;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.repository.UserRoleRepository;
import com.worthyi.worthyi_backend.repository.AvatarRepository;
import com.worthyi.worthyi_backend.repository.VillageInstanceRepository;    
import com.worthyi.worthyi_backend.repository.SocialAccountRepository;
import com.worthyi.worthyi_backend.repository.AvatarInVillageRepository;
import com.worthyi.worthyi_backend.repository.AdultActionInstanceRepository;
import com.worthyi.worthyi_backend.repository.ChildActionInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AvatarRepository avatarRepository;
    private final VillageInstanceRepository villageInstanceRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final AvatarInVillageRepository avatarInVillageRepository;
    private final AdultActionInstanceRepository adultActionInstanceRepository;
    private final ChildActionInstanceRepository childActionInstanceRepository;

    /**
     * userId(실제로는 UUID)로 사용자의 권한 정보를 조회
     * @param userId String 형태의 UUID
     */
    public List<GrantedAuthority> getUserAuthoritiesByUserId(String userId) {
        Optional<User> userOptional = userRepository.findByUserId(UUID.fromString(userId));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // User -> UserRole -> Role 매핑
            List<UserRole> userRoles = userRoleRepository.findByUser_UserId(UUID.fromString(userId));

            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getAuthorityName()))
                    .collect(Collectors.toList());
        } else {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }
    }

    @Transactional(readOnly = true)
    public UserDto.Response getUserInfo(String userId) {
        log.info("getUserInfo - Attempting to fetch user information for userId: {}", userId);
        
        User user = userRepository.findByUserIdWithAvatars(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.error("getUserInfo - User not found for userId: {}", userId);
                    return new IllegalArgumentException(ApiStatus.USER_NOT_FOUND.getMessage());
                });
        
        UserDto.Response response = UserDto.Response.from(user);
        log.info("getUserInfo - Successfully created response DTO for user: {}", response);
        
        return response;
    }

    @Transactional
    public void deleteUserAndRelatedData(UUID userId) {
        log.info("deleteUserAndRelatedData - 사용자와 관련 데이터 삭제 시작. userId: {}", userId);

        // 1. 사용자의 모든 아바타 ID 조회
        List<Long> avatarIds = avatarRepository.findAvatarIdsByUserId(userId);
        log.info("사용자의 아바타 ID 목록 조회 완료: {}", avatarIds);

        // 2. AdultActionInstance 삭제
        adultActionInstanceRepository.deleteByUserId(userId);
        log.info("사용자의 어른 행동 인스턴스 삭제 완료");

        // 3. ChildActionInstance 삭제
        if (!avatarIds.isEmpty()) {
            childActionInstanceRepository.deleteByAvatarIdIn(avatarIds);
            log.info("아바타들의 아이 행동 인스턴스 삭제 완료");
               // 4. AvatarInVillage 삭제
        avatarInVillageRepository.deleteByAvatar_AvatarIdIn(avatarIds);
        log.info("아바타-마을 연결 정보 삭제 완료");
        }

     

        // 5. VillageInstance 삭제
        villageInstanceRepository.deleteByUser_UserId(userId);
        log.info("사용자의 마을 인스턴스 삭제 완료");

        // 6. Avatar 삭제
        avatarRepository.deleteByUser_UserId(userId);
        log.info("사용자의 아바타 삭제 완료");

        // 7. SocialAccount 삭제
        socialAccountRepository.deleteByUser_UserId(userId);
        log.info("사용자의 소셜 계정 정보 삭제 완료");

        // 8. UserRole 삭제
        userRoleRepository.deleteByUser_UserId(userId);
        log.info("사용자의 권한 정보 삭제 완료");

        // 9. User 삭제
        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료");

        log.info("deleteUserAndRelatedData - 사용자와 관련 데이터 삭제 완료. userId: {}", userId);
    }
}