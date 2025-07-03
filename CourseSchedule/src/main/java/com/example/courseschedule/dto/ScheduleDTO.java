package com.example.courseschedule.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
    private Long id;
	private Integer dayOfWeek;
    private String startTime;
    private String endTime;
    private String classroomName;
    private String building;
    private Long teachingClassId;
    private Long classroomId;    // 新增字段，用于关联Classroom实体
    private String courseName;   // 课程名称
    private String classCode;    // 教学班代码
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTeachingClassId() {
		return teachingClassId;
	}
	public void setTeachingClassId(Long teachingClassId) {
		this.teachingClassId = teachingClassId;
	}
	public Integer getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getClassroomName() {
		return classroomName;
	}
	public void setClassroomName(String classroom) {
		this.classroomName = classroom;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public Long getClassroomId() {
		return classroomId;
	}
	public void setClassroomId(Long classroomId) {
		this.classroomId = classroomId;
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
    
}