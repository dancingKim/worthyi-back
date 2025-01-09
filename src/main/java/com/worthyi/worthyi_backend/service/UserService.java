package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.UserDto;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.model.entity.UserRole;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.repository.UserRoleRepository;
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

    /**
     * 이메일을 기반으로 사용자의 권한 정보(GrantedAuthority)를 조회하는 메서드
     *
     * @param providerUserId 사용자 이메일
     * @return 사용자 권한 목록
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생
     */
    public List<GrantedAuthority> getUserAuthoritiesByUserId(String userId) {
        // 1. 이메일로 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByUserId(UUID.fromString(userId));

        // 2. 사용자 정보가 존재하는 경우
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 3. UserRole 테이블에서 사용자 ID에 해당하는 역할(Role) 조회
            List<UserRole> userRoles = userRoleRepository.findByUser_UserId(UUID.fromString(userId));

            // 4. Role 정보를 GrantedAuthority 리스트로 변환하여 반환
            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getAuthorityName()))  // "ROLE_USER", "ROLE_ADMIN" 등
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
}
