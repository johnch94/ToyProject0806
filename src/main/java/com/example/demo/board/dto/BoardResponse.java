package com.example.demo.board.dto;

import com.example.demo.board.BoardEntity;
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
    private String author;
    private Integer viewCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Entity -> Response 변환
    public static BoardResponse fromEntity(BoardEntity entity) {
        return BoardResponse.builder()
                .boardId(entity.getBoardId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .author(entity.getAuthor() != null ? entity.getAuthor().getUsername() : "익명")
                .viewCount(entity.getViewCount())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }
}