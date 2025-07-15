package com.example.courseschedule.dto;

import java.util.List;

/**
 * 教学班详细信息数据传输对象
 * 用于课程详情页中的教学班选择
 */
public class TeachingClassDetailDTO {
    private Long teachingClassId;
    private Long courseId;
    private String courseName;
    private String classCode;
    private String teacherName;
    private Long teacherId;
    private Integer currentStudents;
    private Integer maxStudents;
    private List<ClassScheduleDTO> schedules;
    private Boolean isSelected;
    private String description;

    public TeachingClassDetailDTO() {}

    public TeachingClassDetailDTO(Long teachingClassId, Long courseId, String courseName, 
                                String classCode, String teacherName, Long teacherId,
                                Integer currentStudents, Integer maxStudents,
                                List<ClassScheduleDTO> schedules, Boolean isSelected, String description) {
        this.teachingClassId = teachingClassId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.classCode = classCode;
        this.teacherName = teacherName;
        this.teacherId = teacherId;
        this.currentStudents = currentStudents;
        this.maxStudents = maxStudents;
        this.schedules = schedules;
        this.isSelected = isSelected;
        this.description = description;
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

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRemainingCapacity() {
        return maxStudents - currentStudents;
    }

    public Boolean getIsAvailable() {
        return getRemainingCapacity() > 0 && !isSelected;
    }
}