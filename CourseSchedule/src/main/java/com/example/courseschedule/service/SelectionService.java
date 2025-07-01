package com.example.courseschedule.service;

import com.example.courseschedule.dto.MyCourseDTO;
import com.example.courseschedule.dto.SelectionDTO;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.CourseSelectionRepository;
import com.example.courseschedule.repository.StudentRepository;
import com.example.courseschedule.repository.TeachingClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final StudentRepository studentRepository;

    public SelectionService(CourseSelectionRepository selectionRepository,
                          TeachingClassRepository teachingClassRepository,
                          StudentRepository studentRepository) {
        this.selectionRepository = selectionRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 获取指定学生的已选课程详细列表
     */
    @Transactional(readOnly = true)
    public List<MyCourseDTO> getMyCoursesByStudent(Long studentId) {
        List<CourseSelection> selections = selectionRepository.findByStudentIdWithDetails(studentId);
        return selections.stream()
                .map(selection -> new MyCourseDTO(
                        selection.getId(),
                        selection.getTeachingClass().getId(),
                        selection.getTeachingClass().getCourse().getCourseName(),
                        selection.getTeachingClass().getTeacher().getUser().getRealName(),
                        selection.getTeachingClass().getCourse().getCredit()))
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
     * 转换实体为DTO
     */
    private SelectionDTO convertToDTO(CourseSelection selection) {
        SelectionDTO dto = new SelectionDTO();
        dto.setId(selection.getId());
        dto.setSelectionTime(selection.getSelectionTime());
        dto.setStudentId(selection.getStudent().getId());
        dto.setTeachingClassId(selection.getTeachingClass().getId());
        return dto;
    }
}
