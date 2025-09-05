package com.example.demo.board.dto;

import com.example.demo.board.BoardEntity;
import com.example.demo.user.UserEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {
    
    private Long boardId;
    private String title;
    private String content;

    // 작성자 정보
    private AuthorInfo author;

    private Integer viewCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // 중첩 DTO : AuthorInfo
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long userId;
        private String username;

        public static AuthorInfo fromEntity(UserEntity entity) {
            return AuthorInfo.builder()
                    .userId(entity.getUserId())
                    .username(entity.getUsername())
                    .build();
        }
    }

    public static BoardResponse fromEntity(BoardEntity entity) {
        return BoardResponse.builder()
                .boardId(entity.getBoardId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .author(AuthorInfo.fromEntity(entity.getAuthor()))
                .viewCount(entity.getViewCount())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }
}