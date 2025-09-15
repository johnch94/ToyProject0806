package com.example.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security용 사용자 정보 로드 서비스
 * - 최소한의 구현: username으로 UserEntity 조회
 * - UserEntity를 Spring Security UserDetails로 변환
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        // UserEntity를 Spring Security UserDetails로 변환
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())  // 이미 BCrypt로 암호화된 상태
                .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .build();
    }
}
