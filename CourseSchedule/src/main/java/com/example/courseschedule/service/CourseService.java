package com.example.courseschedule.service;

import com.example.courseschedule.dto.CourseDTO;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final TeachingClassRepository teachingClassRepository;

    public CourseService(CourseRepository courseRepository,
                        TeachingClassRepository teachingClassRepository) {
        this.courseRepository = courseRepository;
        this.teachingClassRepository = teachingClassRepository;
    }

    // 课程相关方法
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setClassCode(courseDTO.getClassCode());
        course.setName(courseDTO.getName());
        course.setCredit(courseDTO.getCredit());
        course.setHours(courseDTO.getHours());
        course.setDescription(courseDTO.getDescription());
        
        Course savedCourse = courseRepository.save(course);
        return convertToCourseDTO(savedCourse);
    }

    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseDTO courseDTO) {
    	Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        course.setName(courseDTO.getName());
        course.setCredit(courseDTO.getCredit());
        course.setHours(courseDTO.getHours());
        course.setDescription(courseDTO.getDescription());
        
        Course updatedCourse = courseRepository.save(course);
        return convertToCourseDTO(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        // 检查是否有教学班关联
        if (teachingClassRepository.existsByCourseId(courseId)) {
            throw new IllegalStateException("Cannot delete course with existing teaching class");
        }
        courseRepository.deleteById(courseId);
    }

    // 转换方法
    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setClassCode(course.getClassCode());
        dto.setName(course.getName());
        dto.setCredit(course.getCredit());
        dto.setHours(course.getHours());
        dto.setDescription(course.getDescription());
        return dto;
    }
}