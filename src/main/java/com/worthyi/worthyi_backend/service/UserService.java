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
}