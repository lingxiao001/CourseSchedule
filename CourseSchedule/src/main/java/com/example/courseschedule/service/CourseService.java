package com.example.courseschedule.service;

import com.example.courseschedule.dto.CourseDTO;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.*;
import com.example.courseschedule.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
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

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        // 对于新建的课程，确保id为null，以触发JPA的创建新实体逻辑
        course.setId(null); 
        return courseRepository.save(course);
    }
    
    public Course update(Long id, Course courseDetails) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + id));

        existingCourse.setName(courseDetails.getName());
        existingCourse.setDescription(courseDetails.getDescription());
        existingCourse.setCredit(courseDetails.getCredit());
        
        return courseRepository.save(existingCourse);
    }

    public void deleteById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new NotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
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