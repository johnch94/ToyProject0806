package com.example.demo.board;

import com.example.demo.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")  // 테이블명 명시
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자
@AllArgsConstructor
@Builder  // 빌더 패턴 추가
@ToString(exclude = {"content"})  // content는 길 수 있으므로 제외
public class BoardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity author;
    
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void updateTitle(String title) {
        this.title = title;
    }
    
    public void updateContent(String content) {
        this.content = content;
    }
    
    // 정적 팩토리 메서드
    public static BoardEntity createBoard(String title, String content, UserEntity author) {
        return BoardEntity.builder()
                .title(title)
                .content(content)
                .author(author)
                .viewCount(0)
                .build();
    }
}