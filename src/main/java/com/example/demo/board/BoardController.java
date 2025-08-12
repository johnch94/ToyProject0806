package com.example.demo.board;

import com.example.demo.board.dto.*;
import com.example.demo.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class BoardController {
    
    private final BoardService boardService;
    
    /**
     * 게시글 전체 조회 (페이징)
     * GET /api/boards?page=0&size=10&sortBy=createdDate
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getAllBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy) {
        
        Page<BoardResponse> boards = boardService.getAllBoards(page, size, sortBy);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 목록 조회 성공", boards)
        );
    }
    
    /**
     * 게시글 상세 조회
     * GET /api/boards/{boardId}
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardById(@PathVariable Long boardId) {
        BoardResponse board = boardService.getBoardById(boardId);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 조회 성공", board)
        );
    }
    
    /**
     * 게시글 생성
     * POST /api/boards
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @Valid @RequestBody BoardCreateRequest request) {
        
        BoardResponse createdBoard = boardService.createBoard(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("게시글 생성 성공", createdBoard));
    }
    
    /**
     * 게시글 수정
     * PUT /api/boards/{boardId}
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request) {
        
        BoardResponse updatedBoard = boardService.updateBoard(boardId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 수정 성공", updatedBoard)
        );
    }
    
    /**
     * 게시글 삭제
     * DELETE /api/boards/{boardId}
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable Long boardId,
            @RequestParam String author) {
        
        boardService.deleteBoard(boardId, author);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 삭제 성공", null)
        );
    }
    
    /**
     * 게시글 검색
     * GET /api/boards/search?keyword=검색어&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> searchBoards(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BoardResponse> boards = boardService.searchBoards(keyword, page, size);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 검색 성공", boards)
        );
    }
    
    /**
     * 작성자별 게시글 조회
     * GET /api/boards/author/{author}?page=0&size=10
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoardsByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BoardResponse> boards = boardService.getBoardsByAuthor(author, page, size);
        
        return ResponseEntity.ok(
            ApiResponse.success("작성자별 게시글 조회 성공", boards)
        );
    }
    
    /**
     * 최신 게시글 10개 조회
     * GET /api/boards/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getRecentBoards() {
        List<BoardResponse> boards = boardService.getRecentBoards();
        
        return ResponseEntity.ok(
            ApiResponse.success("최신 게시글 조회 성공", boards)
        );
    }
    
    /**
     * 인기 게시글 10개 조회
     * GET /api/boards/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getPopularBoards() {
        List<BoardResponse> boards = boardService.getPopularBoards();
        
        return ResponseEntity.ok(
            ApiResponse.success("인기 게시글 조회 성공", boards)
        );
    }
    
    /**
     * 작성자 통계 조회
     * GET /api/boards/stats/author/{author}
     */
    @GetMapping("/stats/author/{author}")
    public ResponseEntity<ApiResponse<AuthorStatsResponse>> getAuthorStats(@PathVariable String author) {
        AuthorStatsResponse stats = boardService.getAuthorStats(author);
        
        return ResponseEntity.ok(
            ApiResponse.success("작성자 통계 조회 성공", stats)
        );
    }
    
    /**
     * 전체 게시글 수 조회
     * GET /api/boards/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalBoardCount() {
        long count = boardService.getTotalBoardCount();
        
        return ResponseEntity.ok(
            ApiResponse.success("전체 게시글 수 조회 성공", count)
        );
    }
    
    /**
     * 게시글 존재 여부 확인
     * GET /api/boards/{boardId}/exists
     */
    @GetMapping("/{boardId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> existsById(@PathVariable Long boardId) {
        boolean exists = boardService.existsById(boardId);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 존재 여부 확인 성공", exists)
        );
    }
    
    /**
     * 헬스 체크
     * GET /api/boards/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Board API 서버 정상 작동", "OK")
        );
    }
}