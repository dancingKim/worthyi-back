package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.model.entity.UserRole;
import com.worthyi.worthyi_backend.repository.UserRepository;
import com.worthyi.worthyi_backend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    // 이메일을 기반으로 권한 정보(GrantedAuthority)를 조회하는 메서드
    public List<GrantedAuthority> getUserAuthoritiesByEmail(String email) {
        // 1. 이메일로 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByEid(email);

        // 2. 사용자 정보가 존재하는 경우
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UUID userUuid = user.getUserUuid();

            // 3. UserRole 테이블에서 사용자 UUID에 해당하는 역할(Role) 조회
            List<UserRole> userRoles = userRoleRepository.findByUser_UserUuid(userUuid);

            // 4. Role 정보를 GrantedAuthority 리스트로 변환하여 반환
            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getAuthorityName()))  // "ROLE_USER", "ROLE_ADMIN" 등
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
}
