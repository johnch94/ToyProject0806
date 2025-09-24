package com.example.demo.common;

import com.example.demo.common.dto.ApiResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 시스템 상태 확인용 컨트롤러
 * Actuator 대신 간단한 헬스체크 제공
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 간단한 헬스체크 엔드포인트
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "ToyProject Demo");
        healthInfo.put("version", "1.0.0");
        
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("서버가 정상적으로 실행 중입니다")
                .data(healthInfo)
                .build();
    }

    /**
     * 더 자세한 상태 정보
     */
    @GetMapping("/detailed")
    public ApiResponse<Map<String, Object>> detailedHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "ToyProject Demo");
        healthInfo.put("version", "1.0.0");
        
        // 시스템 정보 추가
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        systemInfo.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        systemInfo.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        
        healthInfo.put("system", systemInfo);
        
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("시스템 상태 조회 성공")
                .data(healthInfo)
                .build();
    }
}
