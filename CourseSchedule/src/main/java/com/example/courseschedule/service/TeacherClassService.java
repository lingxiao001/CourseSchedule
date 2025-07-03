package com.example.courseschedule.service;

import com.example.courseschedule.dto.TeachingClassDTO;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherClassService {
    private final CourseRepository courseRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final TeacherRepository teacherRepository;
    private final CourseSelectionRepository courseSelectionRepository;

    public TeacherClassService(CourseRepository courseRepository,
                        TeachingClassRepository teachingClassRepository,
                        TeacherRepository teacherRepository,
                        CourseSelectionRepository courseSelectionRepository) {
        this.courseRepository = courseRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.teacherRepository=teacherRepository;
        this.courseSelectionRepository=courseSelectionRepository;
        
    }

    // 教学班相关方法
    public List<TeachingClassDTO> getAllTeachingClasses() {
        List<TeachingClass> classes = teachingClassRepository.findAll();
        return classes.stream()
                .map(this::convertToTeachingClassDTO)
                .collect(Collectors.toList());
    }
    
    public List<TeachingClassDTO> getTeachingClassesByCourse(Long courseId) {
        return teachingClassRepository.findByCourseId(courseId).stream()
                .map(this::convertToTeachingClassDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeachingClassDTO createTeachingClass(Long courseId, TeachingClassDTO teachingClassDTO) {
    	Course course = courseRepository.findById(courseId).orElse(null);
        if (course== null) {
            throw new RuntimeException("Course not found");
        }
        
        Teacher teacher = teacherRepository.findById(teachingClassDTO.getTeacherId()).orElse(null);
        if(teacher==null) {
        	throw new RuntimeException("Teacher not found");
        }
        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setClassCode(teachingClassDTO.getClassCode());
        teachingClass.setMaxStudents(teachingClassDTO.getMaxStudents());
        teachingClass.setCurrentStudents(0); // 初始为0
        teachingClass.setCourse(course);
        teachingClass.setTeacher(teacher);
        
        TeachingClass savedClass = teachingClassRepository.save(teachingClass);
        return convertToTeachingClassDTO(savedClass);
    }

    @Transactional
    public TeachingClassDTO updateTeachingClass(Long classId, TeachingClassDTO teachingClassDTO) {
        TeachingClass teachingClass = teachingClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Teaching class not found"));

        // 更新基本信息
        if (teachingClassDTO.getMaxStudents() != null) {
            teachingClass.setMaxStudents(teachingClassDTO.getMaxStudents());
        }
        
        // 如果提供了teacherId且与当前不同，则更新教师
        if (teachingClassDTO.getTeacherId() != null && 
            !teachingClassDTO.getTeacherId().equals(teachingClass.getTeacher().getId())) {
            Teacher newTeacher = teacherRepository.findById(teachingClassDTO.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            teachingClass.setTeacher(newTeacher);
        }

        TeachingClass updatedClass = teachingClassRepository.save(teachingClass);
        return convertToTeachingClassDTO(updatedClass);
    }

    @Transactional
    public void deleteTeachingClass(Long classId) {
        // 检查是否有选课记录
        if (courseSelectionRepository.existsByTeachingClassId(classId)) {
            throw new IllegalStateException("无法删除已有学生选课的教学班");
        }
        
        // 检查是否有课程表安排
        if (!teachingClassRepository.findById(classId).get().getClassSchedules().isEmpty()) {
            throw new IllegalStateException("Cannot delete teaching class with existing schedules");
        }
        
        teachingClassRepository.deleteById(classId);
    }

    private TeachingClassDTO convertToTeachingClassDTO(TeachingClass teachingClass) {
        TeachingClassDTO dto = new TeachingClassDTO();
        dto.setId(teachingClass.getId());
        dto.setClassCode(teachingClass.getClassCode());
        dto.setMaxStudents(teachingClass.getMaxStudents());
        dto.setCurrentStudents(teachingClass.getCurrentStudents());
        
        if (teachingClass.getCourse() != null) {
            dto.setCourseId(teachingClass.getCourse().getId());
        }
        
        if (teachingClass.getTeacher() != null) {
            dto.setTeacherId(teachingClass.getTeacher().getId());
            if (teachingClass.getTeacher().getUser() != null) {
                dto.setTeacherName(teachingClass.getTeacher().getUser().getRealName());
            }
        }
        
        return dto;
    }
}
