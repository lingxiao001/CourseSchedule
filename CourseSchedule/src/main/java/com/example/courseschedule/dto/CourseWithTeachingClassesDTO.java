package com.example.courseschedule.dto;

import java.util.List;

/**
 * 课程及其教学班分组数据传输对象
 * 用于选课中心按课程显示
 */
public class CourseWithTeachingClassesDTO {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Double credit;
    private String description;
    private List<TeachingClassDetailDTO> teachingClasses;
    private int totalTeachingClasses;
    private int selectedTeachingClasses;

    public CourseWithTeachingClassesDTO() {}

    public CourseWithTeachingClassesDTO(Long courseId, String courseName, String courseCode, 
                                      Double credit, String description, 
                                      List<TeachingClassDetailDTO> teachingClasses) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.credit = credit;
        this.description = description;
        this.teachingClasses = teachingClasses;
        this.totalTeachingClasses = teachingClasses != null ? teachingClasses.size() : 0;
        this.selectedTeachingClasses = teachingClasses != null ? 
            (int) teachingClasses.stream().filter(TeachingClassDetailDTO::getIsSelected).count() : 0;
    }

    // Getters and Setters
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

    public List<TeachingClassDetailDTO> getTeachingClasses() {
        return teachingClasses;
    }

    public void setTeachingClasses(List<TeachingClassDetailDTO> teachingClasses) {
        this.teachingClasses = teachingClasses;
        this.totalTeachingClasses = teachingClasses != null ? teachingClasses.size() : 0;
        this.selectedTeachingClasses = teachingClasses != null ? 
            (int) teachingClasses.stream().filter(TeachingClassDetailDTO::getIsSelected).count() : 0;
    }

    public int getTotalTeachingClasses() {
        return totalTeachingClasses;
    }

    public int getSelectedTeachingClasses() {
        return selectedTeachingClasses;
    }

    public boolean isFullySelected() {
        return selectedTeachingClasses > 0 && selectedTeachingClasses == totalTeachingClasses;
    }

    public boolean isPartiallySelected() {
        return selectedTeachingClasses > 0 && selectedTeachingClasses < totalTeachingClasses;
    }
}