package com.example.demo.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * 사용자명 중복 체크
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);
    
    /**
     * 역할별 사용자 조회 (페이징)
     */
    Page<UserEntity> findByRole(UserRole role, Pageable pageable);
    
    /**
     * 사용자명으로 검색 (부분 일치)
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username LIKE %:keyword%")
    Page<UserEntity> findByUsernameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 특정 기간에 가입한 사용자 조회
     */
    List<UserEntity> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 최근 가입한 사용자 10명 조회
     */
    List<UserEntity> findTop10ByOrderByCreatedDateDesc();
    
    /**
     * 역할별 사용자 수 조회
     */
    long countByRole(UserRole role);
}