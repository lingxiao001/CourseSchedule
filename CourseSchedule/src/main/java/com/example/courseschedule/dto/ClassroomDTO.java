package com.example.courseschedule.dto;

import lombok.Data;

@Data
public class ClassroomDTO {
    private Long id;
    private String building;
    private String classroomName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public String getClassroomName() {
		return classroomName;
	}
	public void setClassroomName(String classroomName) {
		this.classroomName = classroomName;
	}
    
}