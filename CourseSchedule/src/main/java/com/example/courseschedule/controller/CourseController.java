package com.example.courseschedule.controller;

import com.example.courseschedule.dto.CourseDTO;
import com.example.courseschedule.dto.TeachingClassDTO;
import com.example.courseschedule.entity.Course;
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
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course savedCourse = courseService.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        Course updatedCourse = courseService.update(id, courseDetails);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
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