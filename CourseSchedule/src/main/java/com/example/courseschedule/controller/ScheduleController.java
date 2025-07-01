package com.example.courseschedule.controller;

import com.example.courseschedule.dto.ScheduleDTO;
import com.example.courseschedule.service.ScheduleService;
import com.example.courseschedule.utils.XfyunApiClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }
    
    @PostMapping("/teaching-class/{teachingClassId}")
    public ResponseEntity<ScheduleDTO> addSchedule(
            @PathVariable Long teachingClassId,
            @RequestBody ScheduleDTO scheduleDTO) {
        ScheduleDTO savedSchedule = scheduleService.addSchedule(teachingClassId, scheduleDTO);
        return ResponseEntity.ok(savedSchedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleDTO scheduleDTO) {
        ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDTO);
        return ResponseEntity.ok(updatedSchedule);
    }

    @GetMapping("/teaching-class/{teachingClassId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByTeachingClass(
            @PathVariable Long teachingClassId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByTeachingClass(teachingClassId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByTeacher(
            @PathVariable Long teacherId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByTeacher(teacherId);
        return ResponseEntity.ok(schedules);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    //讯飞星火大模型测试
   /* @PostMapping("/invoke-xfyun")
    public ResponseEntity<String> invokeXfyunModel(@RequestBody Map<String, Object> requestData) {
        try {
            // 调用讯飞星火大模型 API
            String response = XfyunApiClient.callXfyunAPI(requestData);

            // 返回响应结果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error invoking XFYUN model: " + e.getMessage());
        }
    }
    */
    
}