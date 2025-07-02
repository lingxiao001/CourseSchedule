package com.example.courseschedule.controller;

import com.example.courseschedule.dto.ClassroomDTO;
import com.example.courseschedule.entity.Classroom;
import com.example.courseschedule.repository.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    @Autowired
    private ClassroomRepository classroomRepository;

    /**
     * 获取所有教室列表
     */
    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll(); // 获取所有教室
        List<ClassroomDTO> classroomDTOs = classrooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classroomDTOs);
    }

    /**
     * 创建新教室
     */
    @PostMapping
    public ResponseEntity<ClassroomDTO> createClassroom(@RequestBody ClassroomDTO classroomDTO) {
        Classroom classroom = new Classroom();
        classroom.setBuilding(classroomDTO.getBuilding());
        classroom.setClassroomName(classroomDTO.getClassroomName());
        classroom.setCapacity(classroomDTO.getCapacity());

        Classroom savedClassroom = classroomRepository.save(classroom);
        return ResponseEntity.ok(convertToDTO(savedClassroom));
    }

    /**
     * 获取指定教室详情
     */
    @GetMapping("/{classroomId}")
    public ResponseEntity<ClassroomDTO> getClassroomById(@PathVariable Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("教室不存在"));
        return ResponseEntity.ok(convertToDTO(classroom));
    }

    /**
     * 更新教室信息
     */
    @PutMapping("/{classroomId}")
    public ResponseEntity<ClassroomDTO> updateClassroom(
            @PathVariable Long classroomId,
            @RequestBody ClassroomDTO classroomDTO) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("教室不存在"));

        classroom.setBuilding(classroomDTO.getBuilding());
        classroom.setClassroomName(classroomDTO.getClassroomName());
        classroom.setCapacity(classroomDTO.getCapacity());

        Classroom updatedClassroom = classroomRepository.save(classroom);
        return ResponseEntity.ok(convertToDTO(updatedClassroom));
    }

    /**
     * 删除教室
     */
    @DeleteMapping("/{classroomId}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long classroomId) {
        classroomRepository.deleteById(classroomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 按教学楼查询教室
     */
    @GetMapping("/building/{building}")
    public ResponseEntity<List<ClassroomDTO>> getClassroomsByBuilding(
            @PathVariable String building) {
        List<Classroom> classrooms = classroomRepository.findByBuilding(building); // 按教学楼查询教室
        List<ClassroomDTO> classroomDTOs = classrooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classroomDTOs);
    }

    /**
     * 查询可用教室
     */
    @GetMapping("/available")
    public ResponseEntity<List<ClassroomDTO>> getAvailableClassrooms(
            @RequestParam Integer dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Classroom> availableClassrooms = classroomRepository.findAvailableClassrooms(
                dayOfWeek, startTime, endTime); // 查询可用教室
        List<ClassroomDTO> classroomDTOs = availableClassrooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classroomDTOs);
    }

    /**
     * 查询指定教学楼中的可用教室
     */
    @GetMapping("/available/{building}")
    public ResponseEntity<List<ClassroomDTO>> getAvailableClassroomsInBuilding(
            @PathVariable String building,
            @RequestParam Integer dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Classroom> availableClassrooms = classroomRepository.findAvailableClassroomsInBuilding(
                building, dayOfWeek, startTime, endTime); // 查询指定教学楼中的可用教室
        List<ClassroomDTO> classroomDTOs = availableClassrooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classroomDTOs);
    }

    /**
     * 将 Classroom 转换为 ClassroomDTO
     */
    private ClassroomDTO convertToDTO(Classroom classroom) {
        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setBuilding(classroom.getBuilding());
        dto.setClassroomName(classroom.getClassroomName());
        dto.setCapacity(classroom.getCapacity());
        return dto;
    }
}
