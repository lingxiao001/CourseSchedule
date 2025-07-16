package com.example.courseschedule.service;

import com.example.courseschedule.dto.*;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.CourseScheduleRepository;
import com.example.courseschedule.repository.CourseSelectionRepository;
import com.example.courseschedule.repository.StudentRepository;
import com.example.courseschedule.repository.TeachingClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.courseschedule.dto.ClassScheduleDTO;

@Service
@Transactional
public class SelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final StudentRepository studentRepository;
    private final CourseScheduleRepository courseScheduleRepository;

    public SelectionService(CourseSelectionRepository selectionRepository,
                          TeachingClassRepository teachingClassRepository,
                          StudentRepository studentRepository,
                          CourseScheduleRepository courseScheduleRepository) {
        this.selectionRepository = selectionRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.studentRepository = studentRepository;
        this.courseScheduleRepository = courseScheduleRepository;
    }

    /**
     * 获取指定学生的已选课程详细列表
     */
    @Transactional(readOnly = true)
    public List<MyCourseDTO> getMyCoursesByStudent(Long studentId) {
        List<CourseSelection> selections = selectionRepository.findByStudentIdWithDetails(studentId);
        return selections.stream()
                .map(selection -> new MyCourseDTO(
                        selection.getTeachingClass().getCourse().getId(), // courseId
                        selection.getTeachingClass().getCourse().getCourseName(), // courseName
                        selection.getTeachingClass().getCourse().getCredit(), // credits
                        selection.getTeachingClass().getTeacher().getUser().getRealName(), // teacherName
                        selection.getTeachingClass().getClassCode(), // classCode
                        selection.getSelectionTime() != null ? selection.getSelectionTime().toString() : null, // selectionTime
                        selection.getTeachingClass().getClassSchedules().stream().map(ClassScheduleDTO::new).collect(Collectors.toList()) // schedules
                ))
                .collect(Collectors.toList());
    }

    /**
     * 学生选课
     */
    @Transactional
    public void selectCourse(Long studentId, Long teachingClassId) {
        // 验证学生存在
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        // 验证教学班存在
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId).orElse(null);
        if (teachingClass == null) {
            throw new RuntimeException("教学班不存在");
        }

        // 检查是否已选
        /*if (selectionRepository.existsByStudentIdAndTeachingClassId(studentId, teachingClassId)) {
            throw new RuntimeException("您已经选过这门课了");
        }*/

        // 检查人数限制
        if (teachingClass.getCurrentStudents() >= teachingClass.getMaxStudents()) {
            throw new RuntimeException("该教学班已满");
        }

        // 冲突检测：遍历该教学班所有上课时间，查找是否有冲突
        List<ClassSchedule> schedules = teachingClass.getClassSchedules();
        for (ClassSchedule schedule : schedules) {
            List<CourseSelection> conflicts = selectionRepository.findConflictingSelections(
                studentId,
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
            );
            if (conflicts != null && !conflicts.isEmpty()) {
                throw new RuntimeException("选课冲突：该时间段已选其他课程！");
            }
        }

        // 创建选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudent(student);
        selection.setTeachingClass(teachingClass);
        selection.setSelectionTime(LocalDateTime.now());

        // 更新教学班人数
        teachingClass.setCurrentStudents(teachingClass.getCurrentStudents() + 1);

        // 保存（级联关系由实体配置自动处理）
        selectionRepository.save(selection);
        teachingClassRepository.save(teachingClass);
    }

    /**
     * 学生退选
     */
    @Transactional
    public void cancelSelection(Long studentId, Long teachingClassId) {
        // 获取选课记录
        CourseSelection selection = selectionRepository.findByStudentIdAndTeachingClassId(studentId, teachingClassId);

        if (selection == null) {
            throw new RuntimeException("未找到选课记录");
        }

        // 获取教学班
        TeachingClass teachingClass = selection.getTeachingClass();

        // 删除选课记录
        selectionRepository.delete(selection);

        // 更新教学班人数
        teachingClass.setCurrentStudents(teachingClass.getCurrentStudents() - 1);
        teachingClassRepository.save(teachingClass);
    }

    /**
     * 根据教学班ID删除所有选课记录（管理员用）
     */
    @Transactional
    public void deleteByTeachingClassId(Long teachingClassId) {
        // 获取教学班
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId).orElse(null);

        if (teachingClass == null) {
            throw new RuntimeException("教学班不存在");
        }

        // 获取选课人数
        int selectionCount = selectionRepository.countByTeachingClassId(teachingClassId);

        // 删除所有关联选课记录
        selectionRepository.deleteByTeachingClassId(teachingClassId);

        // 重置教学班人数
        teachingClass.setCurrentStudents(teachingClass.getCurrentStudents() - selectionCount);
        teachingClassRepository.save(teachingClass);
    }

    /**
     * 根据学生ID删除所有选课记录（管理员用）
     */
    @Transactional
    public void deleteByStudentId(Long studentId) {
        // 获取学生选课列表
        List<CourseSelection> selections = selectionRepository.findByStudentId(studentId);

        if (selections == null || selections.isEmpty()) {
            throw new RuntimeException("未找到选课记录");
        }

        // 更新相关教学班人数
        selections.forEach(selection -> {
            TeachingClass teachingClass = selection.getTeachingClass();
            teachingClass.setCurrentStudents(teachingClass.getCurrentStudents() - 1);
            teachingClassRepository.save(teachingClass);
        });

        // 删除所有选课记录
        selectionRepository.deleteByStudentId(studentId);
    }

    /**
     * 获取所有选课记录(DTO)
     */
    @Transactional(readOnly = true)
    public List<SelectionDTO> getAllSelectionDTOs() {
        return selectionRepository.findAllWithDetails()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据学生ID获取选课记录
     */
    @Transactional(readOnly = true)
    public List<SelectionDTO> getSelectionsByStudent(Long studentId) {
        return selectionRepository.findByStudentIdWithDetails(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据教学班ID获取选课记录
     */
    @Transactional(readOnly = true)
    public List<SelectionDTO> getSelectionsByTeachingClass(Long teachingClassId) {
        return selectionRepository.findByTeachingClassIdWithDetails(teachingClassId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据教学班ID获取教师姓名
     */
    @Transactional(readOnly = true)
    public String getTeacherNameByTeachingClass(Long teachingClassId) {
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId)
                .orElseThrow(() -> new RuntimeException("教学班不存在"));
        
        if (teachingClass.getTeacher() != null && teachingClass.getTeacher().getUser() != null) {
            return teachingClass.getTeacher().getUser().getRealName();
        }
        
        return "未知教师";
    }

    /**
     * 获取学生可选课程列表
     * @param studentId 学生ID
     * @return 可选课程列表
     */
    @Transactional(readOnly = true)
    public List<AvailableCourseDTO> getAvailableCoursesForStudent(Long studentId) {
        // 验证学生存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 获取所有教学班
        List<TeachingClass> allTeachingClasses = teachingClassRepository.findAll();
        
        // 获取学生已选的教学班ID（基于student_id字段）
        List<Long> selectedTeachingClassIds = selectionRepository.findByStudentIdWithDetails(studentId)
                .stream()
                .map(selection -> selection.getTeachingClass().getId())
                .collect(Collectors.toList());

        // 构建可选课程列表
        return allTeachingClasses.stream()
                .map(teachingClass -> {
                    AvailableCourseDTO dto = new AvailableCourseDTO();
                    dto.setTeachingClassId(teachingClass.getId());
                    dto.setCourseId(teachingClass.getCourse().getId());
                    dto.setCourseName(teachingClass.getCourse().getCourseName());
                    dto.setCourseCode(teachingClass.getCourse().getClassCode());
                    dto.setCredit(teachingClass.getCourse().getCredit());
                    dto.setDescription(teachingClass.getCourse().getDescription());
                    
                    // 设置教师信息
                    if (teachingClass.getTeacher() != null && teachingClass.getTeacher().getUser() != null) {
                        dto.setTeacherName(teachingClass.getTeacher().getUser().getRealName());
                        dto.setTeacherId(teachingClass.getTeacher().getId());
                    } else {
                        dto.setTeacherName("未知教师");
                        dto.setTeacherId(0L);
                    }
                    
                    dto.setClassCode(teachingClass.getClassCode());
                    dto.setCurrentStudents(teachingClass.getCurrentStudents());
                    dto.setMaxStudents(teachingClass.getMaxStudents());
                    
                    // 获取该教学班的课程安排并转换为DTO格式
                    List<ClassSchedule> classSchedules = courseScheduleRepository.findByTeachingClassId(teachingClass.getId());
                    List<ClassScheduleDTO> scheduleDTOs = classSchedules.stream()
                            .map(schedule -> new ClassScheduleDTO(
                                    schedule.getDayOfWeek(),
                                    schedule.getStartTime(),
                                    schedule.getEndTime(),
                                    schedule.getClassroom().getClassroomName(),
                                    schedule.getClassroom().getBuilding()
                            ))
                            .collect(Collectors.toList());
                    
                    dto.setSchedules(scheduleDTOs);
                    
                    // 基于student_id判断选课状态
                    dto.setIsSelected(selectedTeachingClassIds.contains(teachingClass.getId()));
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取按课程分组的可选课程列表
     * @param studentId 学生ID
     * @return 按课程分组的可选课程列表
     */
    @Transactional(readOnly = true)
    public List<CourseWithTeachingClassesDTO> getAvailableCoursesGroupedByCourse(Long studentId) {
        // 验证学生存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 获取学生已选的教学班ID（基于student_id字段）
        List<Long> selectedTeachingClassIds = selectionRepository.findByStudentIdWithDetails(studentId)
                .stream()
                .map(selection -> selection.getTeachingClass().getId())
                .collect(Collectors.toList());

        // 获取所有教学班并按课程分组
        List<TeachingClass> allTeachingClasses = teachingClassRepository.findAll();
        
        // 按课程分组
        Map<Course, List<TeachingClass>> groupedByCourse = allTeachingClasses.stream()
                .collect(Collectors.groupingBy(tc -> tc.getCourse()));

        // 构建课程分组DTO
        return groupedByCourse.entrySet().stream()
                .map(entry -> {
                    Course course = entry.getKey();
                    List<TeachingClass> teachingClasses = entry.getValue();

                    // 构建教学班详情列表
                    List<TeachingClassDetailDTO> teachingClassDetails = teachingClasses.stream()
                            .map(teachingClass -> {
                                // 获取该教学班的课程安排
                                List<ClassSchedule> classSchedules = courseScheduleRepository
                                        .findByTeachingClassId(teachingClass.getId());
                                
                                List<ClassScheduleDTO> scheduleDTOs = classSchedules.stream()
                                        .map(schedule -> new ClassScheduleDTO(
                                                schedule.getDayOfWeek(),
                                                schedule.getStartTime(),
                                                schedule.getEndTime(),
                                                schedule.getClassroom().getClassroomName(),
                                                schedule.getClassroom().getBuilding()
                                        ))
                                        .collect(Collectors.toList());

                                return new TeachingClassDetailDTO(
                                        teachingClass.getId(),
                                        course.getId(),
                                        course.getCourseName(),
                                        teachingClass.getClassCode(),
                                        teachingClass.getTeacher() != null && teachingClass.getTeacher().getUser() != null ? 
                                            teachingClass.getTeacher().getUser().getRealName() : "未知教师",
                                        teachingClass.getTeacher() != null ? teachingClass.getTeacher().getId() : 0L,
                                        teachingClass.getCurrentStudents(),
                                        teachingClass.getMaxStudents(),
                                        scheduleDTOs,
                                        selectedTeachingClassIds.contains(teachingClass.getId()),
                                        course.getDescription()
                                );
                            })
                            .collect(Collectors.toList());

                    return new CourseWithTeachingClassesDTO(
                            course.getId(),
                            course.getCourseName(),
                            course.getClassCode(),
                            course.getCredit(),
                            course.getDescription(),
                            teachingClassDetails
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取教师的所有教学班
     * @param teacherId 教师ID
     * @return 教师教学班列表
     */
    @Transactional(readOnly = true)
    public List<MyCourseDTO> getMyCoursesByTeacher(Long teacherId) {
        // 获取教师的所有教学班
        List<TeachingClass> teachingClasses = teachingClassRepository.findByTeacherId(teacherId);
        
        return teachingClasses.stream()
                .map(teachingClass -> new MyCourseDTO(
                        teachingClass.getCourse().getId(),
                        teachingClass.getCourse().getCourseName(),
                        teachingClass.getCourse().getCredit(),
                        teachingClass.getTeacher() != null && teachingClass.getTeacher().getUser() != null ? 
                            teachingClass.getTeacher().getUser().getRealName() : "未知教师",
                        teachingClass.getClassCode(),
                        null, // selectionTime 不适用，因为这不是学生选课
                        teachingClass.getClassSchedules().stream()
                                .map(ClassScheduleDTO::new)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定教学班的所有学生
     * @param teachingClassId 教学班ID
     * @return 学生列表
     */
    @Transactional(readOnly = true)
    public List<SelectionDTO> getStudentsByTeachingClass(Long teachingClassId) {
        List<CourseSelection> selections = selectionRepository.findByTeachingClassIdWithDetails(teachingClassId);
        return selections.stream()
                .map(this::convertToDetailedDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换实体为详细DTO，包含学生姓名等信息
     */
    private SelectionDTO convertToDetailedDTO(CourseSelection selection) {
        SelectionDTO dto = new SelectionDTO();
        
        // 设置基本字段
        dto.setId(selection.getId() != null ? selection.getId() : 0L);
        dto.setSelectionTime(selection.getSelectionTime());
        
        // 安全获取学生信息
        Long studentId = 0L;
        String studentName = "未知学生";
        if (selection.getStudent() != null) {
            studentId = selection.getStudent().getId();
            if (selection.getStudent().getUser() != null) {
                studentName = selection.getStudent().getUser().getRealName();
            }
        }
        dto.setStudentId(studentId);
        dto.setStudentName(studentName);
        
        // 安全获取教学班信息
        Long teachingClassId = 0L;
        String courseName = "未知课程";
        String teacherName = "未知教师";
        
        if (selection.getTeachingClass() != null) {
            TeachingClass teachingClass = selection.getTeachingClass();
            teachingClassId = teachingClass.getId();
            
            if (teachingClass.getCourse() != null) {
                courseName = teachingClass.getCourse().getCourseName();
            }
            
            if (teachingClass.getTeacher() != null && teachingClass.getTeacher().getUser() != null) {
                teacherName = teachingClass.getTeacher().getUser().getRealName();
            }
        }
        dto.setTeachingClassId(teachingClassId);
        dto.setCourseName(courseName);
        dto.setTeacherName(teacherName);
        
        return dto;
    }

    /**
     * 转换实体为DTO（基础版本）
     */
    private SelectionDTO convertToDTO(CourseSelection selection) {
        SelectionDTO dto = new SelectionDTO();
        dto.setId(selection.getId());
        dto.setSelectionTime(selection.getSelectionTime());
        dto.setStudentId(selection.getStudent().getId());
        dto.setTeachingClassId(selection.getTeachingClass().getId());
        return dto;
    }

    /**
     * 管理员获取所有选课记录（带详细信息）
     * @return 所有选课记录列表
     */
    @Transactional(readOnly = true)
    public List<SelectionDTO> getAllSelectionsWithDetails() {
        List<CourseSelection> selections = selectionRepository.findAllWithDetails();
        return selections.stream()
                .map(this::convertToDetailedDTO)
                .collect(Collectors.toList());
    }

    /**
     * 管理员删除单个选课记录
     * @param selectionId 选课记录ID
     */
    @Transactional
    public void deleteSelectionById(Long selectionId) {
        CourseSelection selection = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在"));
        
        TeachingClass teachingClass = selection.getTeachingClass();
        
        // 删除选课记录
        selectionRepository.delete(selection);
        
        // 更新教学班人数
        if (teachingClass != null) {
            teachingClass.setCurrentStudents(teachingClass.getCurrentStudents() - 1);
            teachingClassRepository.save(teachingClass);
        }
    }

    /**
     * 管理员批量删除选课记录
     * @param selectionIds 选课记录ID列表
     */
    @Transactional
    public void deleteSelectionsByIds(List<Long> selectionIds) {
        List<CourseSelection> selections = selectionRepository.findAllById(selectionIds);
        
        if (selections.isEmpty()) {
            throw new RuntimeException("未找到选课记录");
        }
        
        // 按教学班分组，计算每个教学班需要减少的学生数
        Map<TeachingClass, Long> teachingClassCounts = selections.stream()
                .filter(selection -> selection.getTeachingClass() != null)
                .collect(Collectors.groupingBy(
                        CourseSelection::getTeachingClass,
                        Collectors.counting()
                ));
        
        // 删除选课记录
        selectionRepository.deleteAll(selections);
        
        // 更新相关教学班人数
        teachingClassCounts.forEach((teachingClass, count) -> {
            teachingClass.setCurrentStudents(
                    Math.max(0, teachingClass.getCurrentStudents() - count.intValue())
            );
            teachingClassRepository.save(teachingClass);
        });
    }
}
