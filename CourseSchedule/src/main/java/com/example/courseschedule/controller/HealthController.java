package com.example.courseschedule.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "课程管理系统后端服务运行正常");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "服务健康检查通过");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 