package com.example.demo.board;

import com.example.demo.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    
    // 1. 제목으로 검색
    List<BoardEntity> findByTitleContaining(String title);
    
    // 2. 작성자로 검색 (UserEntity 기준)
    List<BoardEntity> findByAuthor(UserEntity author);
    
    // 2-1. 작성자명으로 검색 (Join을 통한 String 검색)
    @Query("SELECT b FROM BoardEntity b WHERE b.author.username = :username")
    List<BoardEntity> findByAuthorUsername(@Param("username") String username);
    
    // 3. 제목 또는 내용으로 검색 (페이징)
    Page<BoardEntity> findByTitleContainingOrContentContaining(
        String title, String content, Pageable pageable);
    
    // 4. 작성자별 게시글 목록 (페이징) - UserEntity 기준
    Page<BoardEntity> findByAuthorOrderByCreatedDateDesc(UserEntity author, Pageable pageable);
    
    // 4-1. 작성자명별 게시글 목록 (페이징) - username 기준  
    @Query("SELECT b FROM BoardEntity b WHERE b.author.username = :username ORDER BY b.createdDate DESC")
    Page<BoardEntity> findByAuthorUsernameOrderByCreatedDateDesc(@Param("username") String username, Pageable pageable);
    
    // 5. 최신 게시글 조회
    List<BoardEntity> findTop10ByOrderByCreatedDateDesc();
    
    // 6. 인기 게시글 조회 (조회수 높은 순)
    List<BoardEntity> findTop10ByOrderByViewCountDesc();
    
    // 7. 특정 기간 게시글 조회
    List<BoardEntity> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 8. 제목 중복 체크
    boolean existsByTitle(String title);
    
    // 9. 작성자의 게시글 수 조회 (UserEntity 기준)
    long countByAuthor(UserEntity author);
    
    // 9-1. 작성자명의 게시글 수 조회 (username 기준)
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.author.username = :username")
    long countByAuthorUsername(@Param("username") String username);
    
    // 10. 커스텀 쿼리 - 조회수 업데이트
    @Modifying
    @Query("UPDATE BoardEntity b SET b.viewCount = b.viewCount + 1 WHERE b.boardId = :boardId")
    int incrementViewCount(@Param("boardId") Long boardId);
    
    // 11. 커스텀 쿼리 - 검색 기능 (제목, 내용, 작성자명)
    @Query("SELECT b FROM BoardEntity b WHERE " +
           "b.title LIKE %:keyword% OR " +
           "b.content LIKE %:keyword% OR " +
           "b.author.username LIKE %:keyword% " +
           "ORDER BY b.createdDate DESC")
    Page<BoardEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 12. 네이티브 쿼리 예시 - 월별 게시글 통계
    @Query(value = "SELECT DATE_FORMAT(created_date, '%Y-%m') as month, COUNT(*) as count " +
                   "FROM boards " +
                   "WHERE created_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                   "GROUP BY DATE_FORMAT(created_date, '%Y-%m') " +
                   "ORDER BY month", 
           nativeQuery = true)
    List<Object[]> getMonthlyPostStatistics();
    
    // 13. 특정 조회수 이상인 게시글
    List<BoardEntity> findByViewCountGreaterThanEqual(Integer viewCount);
    
    // 14. Optional을 사용한 안전한 조회 (UserEntity 기준)
    Optional<BoardEntity> findByBoardIdAndAuthor(Long boardId, UserEntity author);
    
    // 14-1. Optional을 사용한 안전한 조회 (username 기준)
    @Query("SELECT b FROM BoardEntity b WHERE b.boardId = :boardId AND b.author.username = :username")
    Optional<BoardEntity> findByBoardIdAndAuthorUsername(@Param("boardId") Long boardId, @Param("username") String username);
}