package com.example.courseschedule.dto;

import com.example.courseschedule.dto.ClassScheduleDTO;
import java.util.List;

/**
 * 可选课程数据传输对象
 * 用于学生选课功能，包含课程详细信息、教学班信息、已选人数等
 */
public class AvailableCourseDTO {
    private Long teachingClassId;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Double credit;
    private String description;
    private String teacherName;
    private Long teacherId;
    private String classCode;
    private Integer currentStudents;
    private Integer maxStudents;
    private List<ClassScheduleDTO> schedules;
    private Boolean isSelected;

    public AvailableCourseDTO() {}

    public AvailableCourseDTO(Long teachingClassId, Long courseId, String courseName, String courseCode,
                            Double credit, String description, String teacherName, Long teacherId,
                            String classCode, Integer currentStudents, Integer maxStudents,
                            List<ClassScheduleDTO> schedules, Boolean isSelected) {
        this.teachingClassId = teachingClassId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.credit = credit;
        this.description = description;
        this.teacherName = teacherName;
        this.teacherId = teacherId;
        this.classCode = classCode;
        this.currentStudents = currentStudents;
        this.maxStudents = maxStudents;
        this.schedules = schedules;
        this.isSelected = isSelected;
    }

    // Getters and Setters
    public Long getTeachingClassId() {
        return teachingClassId;
    }

    public void setTeachingClassId(Long teachingClassId) {
        this.teachingClassId = teachingClassId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getCurrentStudents() {
        return currentStudents;
    }

    public void setCurrentStudents(Integer currentStudents) {
        this.currentStudents = currentStudents;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public List<ClassScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ClassScheduleDTO> schedules) {
        this.schedules = schedules;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Integer getRemainingCapacity() {
        return maxStudents - currentStudents;
    }

    public Boolean getIsAvailable() {
        return getRemainingCapacity() > 0 && !isSelected;
    }
}