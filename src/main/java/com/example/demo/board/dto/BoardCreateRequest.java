package com.example.demo.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// 1. 게시글 생성 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 5000, message = "내용은 5000자 이내여야 합니다")
    private String content;
    
    // JWT 토큰에서 자동으로 채워지므로 필수 아님
    private String author;
}
