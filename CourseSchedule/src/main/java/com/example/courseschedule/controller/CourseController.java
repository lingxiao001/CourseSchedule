package com.example.courseschedule.controller;

import com.example.courseschedule.dto.CourseDTO;
import com.example.courseschedule.dto.TeachingClassDTO;
import com.example.courseschedule.entity.Teacher;
import com.example.courseschedule.service.CourseService;
import com.example.courseschedule.service.TeacherClassService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final TeacherClassService teacherClassService;
    private final CourseService courseService;

    public CourseController(CourseService courseService,TeacherClassService teacherClassService) {
        this.courseService = courseService;
        this.teacherClassService = teacherClassService;
    }

    // 课程相关接口
    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(createdCourse);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId, 
            @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(courseId, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    // 教学班相关接口
    @GetMapping("/classes")
    public ResponseEntity<List<TeachingClassDTO>> getAllTeachingClasses() {
        List<TeachingClassDTO> classes = teacherClassService.getAllTeachingClasses();
        return ResponseEntity.ok(classes);
    }
    @GetMapping("/{courseId}/classes")
    public List<TeachingClassDTO> getTeachingClassesByCourse(@PathVariable Long courseId) {
        return teacherClassService.getTeachingClassesByCourse(courseId);
    }

    @PostMapping("/{courseId}/classes")
    public ResponseEntity<TeachingClassDTO> createTeachingClass(
            @PathVariable Long courseId,  // 确保这个参数被正确接收
            @RequestBody TeachingClassDTO teachingClassDTO) {
        // 添加验证
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        
        TeachingClassDTO createdClass = teacherClassService.createTeachingClass(courseId, teachingClassDTO);
        return ResponseEntity.ok(createdClass);
    }

    @PutMapping("/classes/{classId}")
    public ResponseEntity<TeachingClassDTO> updateTeachingClass(
            @PathVariable Long classId,
            @RequestBody TeachingClassDTO teachingClassDTO) {
        TeachingClassDTO updatedClass = teacherClassService.updateTeachingClass(classId, teachingClassDTO);
        return ResponseEntity.ok(updatedClass);
    }

    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<Void> deleteTeachingClass(@PathVariable Long classId) {
    	teacherClassService.deleteTeachingClass(classId);
        return ResponseEntity.noContent().build();
    }
}