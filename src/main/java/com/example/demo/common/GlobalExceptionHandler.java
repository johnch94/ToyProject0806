package com.example.demo.common;

import com.example.demo.board.BoardService;
import com.example.demo.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 게시글을 찾을 수 없는 경우
     */
    @ExceptionHandler(BoardService.BoardNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleBoardNotFoundException(
            BoardService.BoardNotFoundException ex, WebRequest request) {
        
        log.error("게시글을 찾을 수 없음: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * 중복된 제목인 경우
     */
    @ExceptionHandler(BoardService.DuplicateTitleException.class)
    public ResponseEntity<ApiResponse<String>> handleDuplicateTitleException(
            BoardService.DuplicateTitleException ex, WebRequest request) {
        
        log.error("중복된 제목: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * 권한이 없는 경우
     */
    @ExceptionHandler(BoardService.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedAccessException(
            BoardService.UnauthorizedAccessException ex, WebRequest request) {
        
        log.error("권한 없음: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * 유효성 검사 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("유효성 검사 실패: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("입력값 검증에 실패했습니다")
                        .data(errors)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
    
    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("예상치 못한 오류 발생: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다"));
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.error("잘못된 인자: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}