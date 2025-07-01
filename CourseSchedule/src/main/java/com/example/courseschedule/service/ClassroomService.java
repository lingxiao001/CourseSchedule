package com.example.courseschedule.service;

import com.example.courseschedule.dto.ClassroomDTO;
import com.example.courseschedule.entity.Classroom;
import com.example.courseschedule.repository.ClassroomRepository;
import com.example.courseschedule.repository.CourseScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final CourseScheduleRepository courseScheduleRepository;

    public ClassroomService(ClassroomRepository classroomRepository,
                            CourseScheduleRepository courseScheduleRepository) {
        this.classroomRepository = classroomRepository;
        this.courseScheduleRepository = courseScheduleRepository;
    }

    @Transactional
    public ClassroomDTO createClassroom(ClassroomDTO classroomDTO) {
        Classroom classroom = new Classroom();
        classroom.setBuilding(classroomDTO.getBuilding());
        classroom.setClassroomName(classroomDTO.getClassroomName());

        Classroom savedClassroom = classroomRepository.save(classroom);
        return convertToDTO(savedClassroom);
    }

    public ClassroomDTO getClassroomById(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("教室不存在"));
        return convertToDTO(classroom);
    }

    @Transactional
    public ClassroomDTO updateClassroom(Long classroomId, ClassroomDTO classroomDTO) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("教室不存在"));

        classroom.setBuilding(classroomDTO.getBuilding());
        classroom.setClassroomName(classroomDTO.getClassroomName());

        Classroom updatedClassroom = classroomRepository.save(classroom);
        return convertToDTO(updatedClassroom);
    }

   /* @Transactional
    public void deleteClassroom(Long classroomId) {
        if (courseScheduleRepository.existsByClassroomId(classroomId)) {
            throw new RuntimeException("无法删除：该教室已有排课记录");
        }
        classroomRepository.deleteById(classroomId);
    }

    public List<ClassroomDTO> getClassroomsByBuilding(String building) {
        return classroomRepository.findByBuilding(building).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }*/

    public List<ClassroomDTO> getAvailableClassrooms(Integer dayOfWeek, String startTime, String endTime) {
        return classroomRepository.findAvailableClassrooms(dayOfWeek, startTime, endTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassroomDTO> getAvailableClassroomsInBuilding(
            String building, Integer dayOfWeek, String startTime, String endTime) {
        return classroomRepository.findAvailableClassroomsInBuilding(
                building, dayOfWeek, startTime, endTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ClassroomDTO convertToDTO(Classroom classroom) {
        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setBuilding(classroom.getBuilding());
        dto.setClassroomName(classroom.getClassroomName());
        return dto;
    }
}
