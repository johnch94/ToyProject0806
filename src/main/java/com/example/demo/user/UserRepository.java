package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * 사용자명으로 사용자 조회
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 사용자명 중복 체크
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);


}
