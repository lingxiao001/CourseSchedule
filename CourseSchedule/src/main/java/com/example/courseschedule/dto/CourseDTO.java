package com.example.courseschedule.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private Double credit;
    private String classCode;
    private Integer maxStudents;
    private Integer currentStudents;
    private Integer hours;
    private String description;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getCredit() {
		return credit;
	}
	public void setCredit(Double credit) {
		this.credit = credit;
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
	public Integer getHours() {
		return hours;
	}
	public void setHours(Integer hours) {
		this.hours = hours;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
}