package com.example.courseschedule.controller;

import com.example.courseschedule.dto.ScheduleDTO;
import com.example.courseschedule.entity.ClassSchedule;
import com.example.courseschedule.entity.Classroom;
import com.example.courseschedule.entity.TeachingClass;
import com.example.courseschedule.repository.ClassroomRepository;
import com.example.courseschedule.repository.CourseScheduleRepository;
import com.example.courseschedule.repository.TeachingClassRepository;
import com.example.courseschedule.service.BasicIntelligentSchedulingService;
import com.example.courseschedule.service.BasicIntelligentSchedulingService.SchedulingMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/intelligent-scheduling")
public class IntelligentScheduleController {
    
    private final BasicIntelligentSchedulingService basicIntelligentSchedulingService;
    private final TeachingClassRepository teachingClassRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    public IntelligentScheduleController(BasicIntelligentSchedulingService basicIntelligentSchedulingService, TeachingClassRepository teachingClassRepository, ClassroomRepository classroomRepository, CourseScheduleRepository courseScheduleRepository) {
        this.basicIntelligentSchedulingService = basicIntelligentSchedulingService;
        this.teachingClassRepository=teachingClassRepository;
		this.classroomRepository = classroomRepository;
		this.courseScheduleRepository = courseScheduleRepository;
    }

    /**
     * 自动排课接口
     * @param teachingClassId 教学班ID
     * @param mode 排课模式 (RULE_BASED 或 AI_BASED)
     * @return 排课结果
     */
    @GetMapping("/ai-prompt/{teachingClassId}")
    public ResponseEntity<String> getAIPrompt(@PathVariable Long teachingClassId) {
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId)
            .orElseThrow(() -> new RuntimeException("教学班不存在"));
        
        List<Classroom> classrooms = classroomRepository.findAll();
        List<ClassSchedule> existingSchedules = courseScheduleRepository.findAll();
        
        String prompt = basicIntelligentSchedulingService.buildAIPrompt(teachingClass, classrooms, existingSchedules);
        return ResponseEntity.ok(prompt);
    }		 
    		 
    @PostMapping("/auto-schedule")
    public ResponseEntity<ClassSchedule> autoSchedule(
            @RequestParam Long teachingClassId,
            @RequestParam(defaultValue = "AI_BASED") SchedulingMode mode) {
        ClassSchedule schedule = basicIntelligentSchedulingService.autoSchedule(teachingClassId, mode);
        return ResponseEntity.ok(schedule);
    }

    /**
     * 基于规则的排课
     */
    @PostMapping("/rule-based")
    public ResponseEntity<ClassSchedule> ruleBasedSchedule(@RequestParam Long teachingClassId) {
        ClassSchedule schedule = basicIntelligentSchedulingService.ruleBasedSchedule(teachingClassId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * 基于AI的排课
     */
    @PostMapping("/ai-based")
    public ResponseEntity<ClassSchedule> aiBasedSchedule(
            @RequestBody Map<String, Object> requestBody) {
        Long teachingClassId = Long.parseLong(requestBody.get("teachingClassId").toString());
        ClassSchedule schedule = basicIntelligentSchedulingService.aiBasedSchedule(teachingClassId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * 获取教学班所有可能的排课时间段
     */
    @GetMapping("/possible-schedules/{teachingClassId}")
    public ResponseEntity<List<ScheduleDTO>> findPossibleSchedules(
            @PathVariable Long teachingClassId) {
        List<ClassSchedule> schedules = basicIntelligentSchedulingService.findPossibleSchedules(teachingClassId);
        
        // Convert to DTO format with null checks
        List<ScheduleDTO> dtos = schedules.stream().map(schedule -> {
            ScheduleDTO dto = new ScheduleDTO();
            dto.setDayOfWeek(schedule.getDayOfWeek());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            
            // Add null checks for classroom
            if (schedule.getClassroom() != null) {
                dto.setClassroomName(schedule.getClassroom().getClassroomName());
                dto.setBuilding(schedule.getClassroom().getBuilding());
                dto.setClassroomId(schedule.getClassroom().getId());
            } else {
                dto.setClassroomName("N/A");
                dto.setBuilding("N/A");
                dto.setClassroomId(null);
            }
            
            dto.setTeachingClassId(schedule.getTeachingClass().getId());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * 批量规则排课：一次为一个教学班生成多节课
     */
    @PostMapping("/rule-based-batch")
    public ResponseEntity<List<ClassSchedule>> ruleBasedScheduleBatch(
            @RequestParam Long teachingClassId,
            @RequestParam(defaultValue = "2") Integer lessonsPerWeek) {
        List<ClassSchedule> schedules = basicIntelligentSchedulingService.ruleBasedScheduleBatch(teachingClassId, lessonsPerWeek);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 冲突检测接口
     */
    @GetMapping("/conflicts")
    public ResponseEntity<List<com.example.courseschedule.dto.ConflictDTO>> detectConflicts(
            @RequestParam Long teachingClassId,
            @RequestParam Integer dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Long classroomId) {
        List<com.example.courseschedule.dto.ConflictDTO> list = basicIntelligentSchedulingService.detectConflicts(
                teachingClassId, dayOfWeek, startTime, endTime, classroomId);
        return ResponseEntity.ok(list);
    }
}