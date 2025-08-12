package com.example.demo.board;

import com.example.demo.board.dto.*;
import com.example.demo.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardService {
    
    private final BoardRepository boardRepository;
    
    /**
     * 게시글 전체 조회 (페이징)
     */
    public Page<BoardResponse> getAllBoards(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return boardRepository.findAll(pageable)
                .map(BoardResponse::fromEntity);
    }
    
    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public BoardResponse getBoardById(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + boardId));
        
        // 조회수 증가
        board.incrementViewCount();
        log.info("게시글 조회 - ID: {}, 현재 조회수: {}", boardId, board.getViewCount());
        
        return BoardResponse.fromEntity(board);
    }
    
    /**
     * 게시글 생성
     */
    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        // 제목 중복 체크
        if (boardRepository.existsByTitle(request.getTitle())) {
            throw new DuplicateTitleException("이미 존재하는 제목입니다: " + request.getTitle());
        }
        
        BoardEntity board = BoardEntity.createBoard(
            request.getTitle(),
            request.getContent(),
            request.getAuthor()
        );
        
        BoardEntity savedBoard = boardRepository.save(board);
        log.info("게시글 생성 완료 - ID: {}, 제목: {}", savedBoard.getBoardId(), savedBoard.getTitle());
        
        return BoardResponse.fromEntity(savedBoard);
    }
    
    /**
     * 게시글 수정
     */
    @Transactional
    public BoardResponse updateBoard(Long boardId, BoardUpdateRequest request) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + boardId));
        
        // 작성자 확인
        if (!board.getAuthor().equals(request.getAuthor())) {
            throw new UnauthorizedAccessException("게시글 수정 권한이 없습니다.");
        }
        
        board.updateTitle(request.getTitle());
        board.updateContent(request.getContent());
        
        log.info("게시글 수정 완료 - ID: {}", boardId);
        return BoardResponse.fromEntity(board);
    }
    
    /**
     * 게시글 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId, String author) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + boardId));
        
        // 작성자 확인
        if (!board.getAuthor().equals(author)) {
            throw new UnauthorizedAccessException("게시글 삭제 권한이 없습니다.");
        }
        
        boardRepository.delete(board);
        log.info("게시글 삭제 완료 - ID: {}", boardId);
    }
    
    /**
     * 키워드로 게시글 검색 (페이징)
     */
    public Page<BoardResponse> searchBoards(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boardRepository.findByKeyword(keyword, pageable)
                .map(BoardResponse::fromEntity);
    }
    
    /**
     * 작성자별 게시글 조회
     */
    public Page<BoardResponse> getBoardsByAuthor(String author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boardRepository.findByAuthorOrderByCreatedDateDesc(author, pageable)
                .map(BoardResponse::fromEntity);
    }
    
    /**
     * 최신 게시글 10개 조회
     */
    public List<BoardResponse> getRecentBoards() {
        return boardRepository.findTop10ByOrderByCreatedDateDesc()
                .stream()
                .map(BoardResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 인기 게시글 10개 조회 (조회수 기준)
     */
    public List<BoardResponse> getPopularBoards() {
        return boardRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(BoardResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 작성자 통계 조회
     */
    public AuthorStatsResponse getAuthorStats(String author) {
        long totalPosts = boardRepository.countByAuthor(author);
        List<BoardEntity> recentPosts = boardRepository.findByAuthor(author);
        
        return AuthorStatsResponse.builder()
                .author(author)
                .totalPosts(totalPosts)
                .recentPostCount(recentPosts.size())
                .build();
    }
    
    /**
     * 게시글 존재 여부 확인
     */
    public boolean existsById(Long boardId) {
        return boardRepository.existsById(boardId);
    }
    
    /**
     * 전체 게시글 수 조회
     */
    public long getTotalBoardCount() {
        return boardRepository.count();
    }
    
    /**
     * 특정 기간 게시글 조회
     */
    public List<BoardResponse> getBoardsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return boardRepository.findByCreatedDateBetween(startDate, endDate)
                .stream()
                .map(BoardResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 커스텀 예외 클래스들
    public static class BoardNotFoundException extends RuntimeException {
        public BoardNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class DuplicateTitleException extends RuntimeException {
        public DuplicateTitleException(String message) {
            super(message);
        }
    }
    
    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }
}