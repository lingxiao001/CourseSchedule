package com.example.courseschedule.controller;

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
}