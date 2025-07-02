package com.example.courseschedule.controller;

import com.example.courseschedule.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TeachingClassRepository teachingClassRepository;

    @GetMapping
    public Map<String, Long> getStats() {
        Map<String, Long> res = new HashMap<>();
        res.put("userCount", userRepository.count());
        res.put("studentCount", studentRepository.count());
        res.put("teacherCount", teacherRepository.count());
        res.put("courseCount", courseRepository.count());
        res.put("classroomCount", classroomRepository.count());
        res.put("teachingClassCount", teachingClassRepository.count());
        return res;
    }
} 