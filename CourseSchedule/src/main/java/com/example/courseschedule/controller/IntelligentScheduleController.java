package com.example.courseschedule.controller;

import com.example.courseschedule.entity.ClassSchedule;
import com.example.courseschedule.service.BasicIntelligentSchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/intelligent-scheduling")
public class IntelligentScheduleController {
    private final BasicIntelligentSchedulingService basicIntelligentSchedulingService;

    public IntelligentScheduleController(BasicIntelligentSchedulingService basicIntelligentSchedulingService) {
        this.basicIntelligentSchedulingService = basicIntelligentSchedulingService;
    }

    /**
     * 全局智能排课接口（遗传算法）
     * 一键为所有教学班排课
     */
    @PostMapping("/auto-schedule")
    public ResponseEntity<List<ClassSchedule>> autoSchedule(HttpServletRequest request) {
        // 添加日志记录，便于调试
        System.out.println("=== 全局智能排课接口被调用 ===");
        System.out.println("调用时间: " + new java.util.Date());
        System.out.println("请求来源: " + request.getRemoteAddr());
        
        List<ClassSchedule> schedules = basicIntelligentSchedulingService.autoSchedule();
        return ResponseEntity.ok(schedules);
    }

    /**
     * 单个教学班智能排课接口
     * @param teachingClassId 教学班ID
     * @return 该班级排课结果
     */
    @PostMapping("/auto-schedule/{teachingClassId}")
    public ResponseEntity<List<ClassSchedule>> autoScheduleForTeachingClass(@PathVariable Long teachingClassId) {
        List<ClassSchedule> schedules = basicIntelligentSchedulingService.autoScheduleForTeachingClass(teachingClassId);
        return ResponseEntity.ok(schedules);
    }
}