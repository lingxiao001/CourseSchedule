package com.example.courseschedule.service;

import com.example.courseschedule.dto.ScheduleDTO;
import com.example.courseschedule.entity.ClassSchedule;
import com.example.courseschedule.entity.Classroom;
import com.example.courseschedule.entity.TeachingClass;
import com.example.courseschedule.exception.ConflictException;
import com.example.courseschedule.exception.NotFoundException;
import com.example.courseschedule.repository.ClassroomRepository;
import com.example.courseschedule.repository.CourseScheduleRepository;
import com.example.courseschedule.repository.TeachingClassRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduleService {
	private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);
    private final ClassroomRepository classroomRepository;
    private final CourseScheduleRepository scheduleRepository;
    private final TeachingClassRepository teachingClassRepository;

    public ScheduleService(CourseScheduleRepository scheduleRepository,
                         TeachingClassRepository teachingClassRepository,
                         ClassroomRepository classroomRepository) {
        this.scheduleRepository = scheduleRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.classroomRepository = classroomRepository;
    }

    /**
     * 获取所有课程安排
     */
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAllByOrderByDayOfWeekAscStartTimeAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 为教学班添加课程安排
     */
    @Transactional
    public ScheduleDTO addSchedule(Long teachingClassId, ScheduleDTO scheduleDTO) {
        log.info("Adding schedule for teaching class {}: {}", teachingClassId, scheduleDTO);
        
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId)
                .orElseThrow(() -> new NotFoundException("教学班不存在: " + teachingClassId));

        validateScheduleDTO(scheduleDTO);
        checkAllConflicts(teachingClassId, scheduleDTO);

        Classroom classroom = findOrCreateClassroom(scheduleDTO);
        ClassSchedule classSchedule = buildClassSchedule(teachingClass, classroom, scheduleDTO);

        ClassSchedule savedSchedule = scheduleRepository.save(classSchedule);
        log.info("Schedule added successfully: {}", savedSchedule.getId());
        
        return convertToDTO(savedSchedule);
    }

    /**
     * 更新课程安排
     */
    @Transactional
    public ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDTO) {
        log.info("Updating schedule {}: {}", scheduleId, scheduleDTO);
        
        ClassSchedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("课程安排不存在: " + scheduleId));

        validateScheduleDTO(scheduleDTO);
        checkAllConflicts(existingSchedule.getTeachingClass().getId(), scheduleDTO, scheduleId);

        Classroom classroom = findOrCreateClassroom(scheduleDTO);
        updateExistingSchedule(existingSchedule, classroom, scheduleDTO);

        ClassSchedule updatedSchedule = scheduleRepository.save(existingSchedule);
        log.info("Schedule updated successfully: {}", scheduleId);
        
        return convertToDTO(updatedSchedule);
    }

    /**
     * 检查所有冲突（包括教学班、教师和教室）
     */
    private void checkAllConflicts(Long teachingClassId, ScheduleDTO scheduleDTO, Long... excludeScheduleId) {
        checkTeachingClassConflict(teachingClassId, scheduleDTO, excludeScheduleId);
        checkTeacherConflict(teachingClassId, scheduleDTO, excludeScheduleId);
        checkClassroomConflict(scheduleDTO, excludeScheduleId);
    }

    /**
     * 检查教学班时间冲突
     */
    private void checkTeachingClassConflict(Long teachingClassId, ScheduleDTO scheduleDTO, Long... excludeScheduleId) {
        boolean conflictExists = excludeScheduleId.length > 0 ?
                scheduleRepository.existsTimeConflictExcludingSelf(
                        teachingClassId,
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime(),
                        excludeScheduleId[0]) :
                scheduleRepository.existsTimeConflict(
                        teachingClassId,
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime());

        if (conflictExists) {
            throw new ConflictException("该教学班在此时间段已有其他课程安排");
        }
    }

    /**
     * 检查教师时间冲突
     */
    private void checkTeacherConflict(Long teachingClassId, ScheduleDTO scheduleDTO, Long... excludeScheduleId) {
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId)
                .orElseThrow(() -> new NotFoundException("教学班不存在: " + teachingClassId));

        boolean conflictExists = excludeScheduleId.length > 0 ?
                scheduleRepository.existsTeacherTimeConflictExcludingSelf(
                        teachingClass.getTeacher().getId(),
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime(),
                        excludeScheduleId[0]) :
                scheduleRepository.existsTeacherTimeConflict(
                        teachingClass.getTeacher().getId(),
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime());

        if (conflictExists) {
            throw new ConflictException("该教师在此时间段已有其他课程安排");
        }
    }

    /**
     * 检查教室时间冲突
     */
    private void checkClassroomConflict(ScheduleDTO scheduleDTO, Long... excludeScheduleId) {
        log.debug("Checking classroom conflict for: {} - {}", 
                scheduleDTO.getBuilding(), scheduleDTO.getClassroomName());

        Classroom classroom;
        if (scheduleDTO.getClassroomId() != null) {
            classroom = classroomRepository.findById(scheduleDTO.getClassroomId())
                    .orElseThrow(() -> new NotFoundException("教室不存在: " + scheduleDTO.getClassroomId()));
        } else {
            classroom = classroomRepository.findByBuildingAndClassroomName(
                    scheduleDTO.getBuilding(),
                    scheduleDTO.getClassroomName()
            ).orElseThrow(() -> new NotFoundException("教室不存在: " +
                    scheduleDTO.getBuilding() + " - " + scheduleDTO.getClassroomName()));
        }

        boolean conflictExists = excludeScheduleId.length > 0 ?
                scheduleRepository.existsClassroomTimeConflictExcludingSelf(
                        classroom.getId(),
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime(),
                        excludeScheduleId[0]) :
                scheduleRepository.existsClassroomTimeConflict(
                        classroom.getId(),
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime());

        if (conflictExists) {
            throw new ConflictException("该教室在此时间段已被占用");
        }
    }

    /**
     * 查找或创建教室
     * 如果前端直接传递了 classroomId，则直接根据 ID 查找；否则按照教学楼 + 教室名称匹配，若不存在则创建。
     */
    private Classroom findOrCreateClassroom(ScheduleDTO scheduleDTO) {
        if (scheduleDTO.getClassroomId() != null) {
            // 根据 ID 查找教室
            return classroomRepository.findById(scheduleDTO.getClassroomId())
                    .orElseThrow(() -> new NotFoundException("教室不存在: " + scheduleDTO.getClassroomId()));
        }

        // 若未提供 ID，则按楼栋 + 教室名称查找 / 创建
        return classroomRepository.findByBuildingAndClassroomName(
                scheduleDTO.getBuilding(),
                scheduleDTO.getClassroomName()
        ).orElseGet(() -> {
            Classroom newClassroom = new Classroom();
            newClassroom.setBuilding(scheduleDTO.getBuilding());
            newClassroom.setClassroomName(scheduleDTO.getClassroomName());
            return classroomRepository.save(newClassroom);
        });
    }

    /**
     * 构建新的课程安排实体
     */
    private ClassSchedule buildClassSchedule(TeachingClass teachingClass, Classroom classroom, ScheduleDTO scheduleDTO) {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setDayOfWeek(scheduleDTO.getDayOfWeek());
        classSchedule.setStartTime(scheduleDTO.getStartTime());
        classSchedule.setEndTime(scheduleDTO.getEndTime());
        classSchedule.setClassroom(classroom);
        classSchedule.setTeachingClass(teachingClass);
        return classSchedule;
    }

    /**
     * 更新现有课程安排实体
     */
    private void updateExistingSchedule(ClassSchedule existingSchedule, Classroom classroom, ScheduleDTO scheduleDTO) {
        existingSchedule.setDayOfWeek(scheduleDTO.getDayOfWeek());
        existingSchedule.setStartTime(scheduleDTO.getStartTime());
        existingSchedule.setEndTime(scheduleDTO.getEndTime());
        existingSchedule.setClassroom(classroom);
    }

    /**
     * 验证ScheduleDTO
     */
    private void validateScheduleDTO(ScheduleDTO scheduleDTO) {
        if (scheduleDTO.getClassroomId() == null) {
            // 未提供教室ID时，需要提供教学楼与教室名称
            if (scheduleDTO.getClassroomName() == null || scheduleDTO.getClassroomName().isEmpty()) {
                throw new IllegalArgumentException("教室名称不能为空");
            }
            if (scheduleDTO.getBuilding() == null || scheduleDTO.getBuilding().isEmpty()) {
                throw new IllegalArgumentException("教学楼名称不能为空");
            }
        }
        if (scheduleDTO.getDayOfWeek() == null || scheduleDTO.getDayOfWeek() < 1 || scheduleDTO.getDayOfWeek() > 7) {
            throw new IllegalArgumentException("星期几必须为1-7之间的数字");
        }
        if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
    }

    /**
     * 获取教学班的课程安排
     */
    public List<ScheduleDTO> getSchedulesByTeachingClass(Long teachingClassId) {
        return scheduleRepository.findByTeachingClassId(teachingClassId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取教师的课程安排
     */
    public List<ScheduleDTO> getSchedulesByTeacher(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除课程安排
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new NotFoundException("课程安排不存在: " + scheduleId);
        }
        scheduleRepository.deleteById(scheduleId);
        log.info("Schedule deleted: {}", scheduleId);
    }

    /**
     * 批量删除全校排课
     */
    @Transactional
    public void deleteAllSchedules() {
        scheduleRepository.deleteAll();
    }

    /**
     * 将ClassSchedule实体转换为ScheduleDTO
     */
    private ScheduleDTO convertToDTO(ClassSchedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId()); 
        dto.setClassroomId(schedule.getClassroom().getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        
        if (schedule.getClassroom() != null) {
            dto.setBuilding(schedule.getClassroom().getBuilding());
            dto.setClassroomName(schedule.getClassroom().getClassroomName());
            dto.setClassroomId(schedule.getClassroom().getId());
        }
        
        if (schedule.getTeachingClass() != null) {
            dto.setTeachingClassId(schedule.getTeachingClass().getId());
            dto.setClassCode(schedule.getTeachingClass().getClassCode());
            
            // 获取课程名称
            if (schedule.getTeachingClass().getCourse() != null) {
                dto.setCourseName(schedule.getTeachingClass().getCourse().getCourseName());
            }
        }

        return dto;
    }
}