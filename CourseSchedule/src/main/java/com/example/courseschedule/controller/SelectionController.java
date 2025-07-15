package com.example.courseschedule.controller;

import com.example.courseschedule.dto.AvailableCourseDTO;
import com.example.courseschedule.dto.CourseWithTeachingClassesDTO;
import com.example.courseschedule.dto.MyCourseDTO;
import com.example.courseschedule.dto.SelectionDTO;
import com.example.courseschedule.service.SelectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selections")
public class SelectionController {
    private final SelectionService selectionService;

    public SelectionController(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @GetMapping("/dto")
    public ResponseEntity<List<SelectionDTO>> getAllSelectionDTOs() {
        List<SelectionDTO> selections = selectionService.getAllSelectionDTOs();
        return ResponseEntity.ok(selections);
    }

    @PostMapping
    public ResponseEntity<String> selectCourse(
            @RequestParam Long studentId,
            @RequestParam Long teachingClassId) {
        try {
            selectionService.selectCourse(studentId, teachingClassId);
            return ResponseEntity.ok("选课成功");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<String> cancelSelection(
            @RequestParam Long studentId,
            @RequestParam Long teachingClassId) {
        try {
            selectionService.cancelSelection(studentId, teachingClassId);
            return ResponseEntity.ok("退选成功");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SelectionDTO>> getSelectionsByStudent(
            @PathVariable Long studentId) {
        List<SelectionDTO> selections = selectionService.getSelectionsByStudent(studentId);
        return ResponseEntity.ok(selections);
    }

    @GetMapping("/teaching-class/{teachingClassId}")
    public ResponseEntity<List<SelectionDTO>> getSelectionsByTeachingClass(
            @PathVariable Long teachingClassId) {
        List<SelectionDTO> selections = selectionService.getSelectionsByTeachingClass(teachingClassId);
        return ResponseEntity.ok(selections);
    }

    @GetMapping("/my-courses/student/{studentId}")
    public ResponseEntity<List<MyCourseDTO>> getMyCourses(@PathVariable Long studentId) {
        List<MyCourseDTO> myCourses = selectionService.getMyCoursesByStudent(studentId);
        return ResponseEntity.ok(myCourses);
    }
    
    @GetMapping("/teacher-name/{teachingClassId}")
    public ResponseEntity<String> getTeacherName(@PathVariable Long teachingClassId) {
        String teacherName = selectionService.getTeacherNameByTeachingClass(teachingClassId);
        return ResponseEntity.ok(teacherName);
    }

    /**
     * 获取学生可选课程列表
     * @param studentId 学生ID
     * @return 可选课程列表
     */
    @GetMapping("/available-courses/student/{studentId}")
    public ResponseEntity<List<AvailableCourseDTO>> getAvailableCourses(@PathVariable Long studentId) {
        try {
            List<AvailableCourseDTO> availableCourses = selectionService.getAvailableCoursesForStudent(studentId);
            return ResponseEntity.ok(availableCourses);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * 获取按课程分组的可选课程列表
     * @param studentId 学生ID
     * @return 按课程分组的可选课程列表
     */
    @GetMapping("/available-courses-by-course/student/{studentId}")
    public ResponseEntity<List<CourseWithTeachingClassesDTO>> getAvailableCoursesGroupedByCourse(@PathVariable Long studentId) {
        try {
            List<CourseWithTeachingClassesDTO> courses = selectionService.getAvailableCoursesGroupedByCourse(studentId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }
}