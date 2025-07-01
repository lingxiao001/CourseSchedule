package com.example.courseschedule.dto;

import lombok.Data;

@Data
public class TeachingClassDTO {
    private Long id;
    private String classCode;
    private Integer maxStudents;
    private Integer currentStudents;
    private Long courseId;
    private Long teacherId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClassCode() {
		return classCode;
	}
	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}
	public Integer getMaxStudents() {
		return maxStudents;
	}
	public void setMaxStudents(Integer maxStudents) {
		this.maxStudents = maxStudents;
	}
	public Integer getCurrentStudents() {
		return currentStudents;
	}
	public void setCurrentStudents(Integer currentStudents) {
		this.currentStudents = currentStudents;
	}
	public Long getCourseId() {
		return courseId;
	}
	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
	public Long getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}
    
}