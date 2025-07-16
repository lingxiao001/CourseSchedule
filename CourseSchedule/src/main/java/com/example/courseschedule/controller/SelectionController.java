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

    @GetMapping("/my-courses/teacher/{teacherId}")
    public ResponseEntity<List<MyCourseDTO>> getMyCoursesByTeacher(@PathVariable Long teacherId) {
        List<MyCourseDTO> myTeachingClasses = selectionService.getMyCoursesByTeacher(teacherId);
        return ResponseEntity.ok(myTeachingClasses);
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

    /**
     * 获取指定教学班的所有学生
     * @param teachingClassId 教学班ID
     * @return 学生列表
     */
    @GetMapping("/students/teaching-class/{teachingClassId}")
    public ResponseEntity<List<SelectionDTO>> getStudentsByTeachingClass(@PathVariable Long teachingClassId) {
        try {
            List<SelectionDTO> students = selectionService.getStudentsByTeachingClass(teachingClassId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * 管理员获取所有选课记录
     * @return 所有选课记录列表
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<SelectionDTO>> getAllSelections() {
        try {
            List<SelectionDTO> selections = selectionService.getAllSelectionsWithDetails();
            return ResponseEntity.ok(selections);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 管理员删除选课记录
     * @param selectionId 选课记录ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/{selectionId}")
    public ResponseEntity<String> deleteSelection(@PathVariable Long selectionId) {
        try {
            selectionService.deleteSelectionById(selectionId);
            return ResponseEntity.ok("选课记录删除成功");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("删除失败: " + e.getMessage());
        }
    }

    /**
     * 管理员批量删除选课记录
     * @param selectionIds 选课记录ID列表
     * @return 删除结果
     */
    @PostMapping("/admin/batch-delete")
    public ResponseEntity<String> deleteSelections(@RequestBody List<Long> selectionIds) {
        try {
            selectionService.deleteSelectionsByIds(selectionIds);
            return ResponseEntity.ok("批量删除成功");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("批量删除失败: " + e.getMessage());
        }
    }
}