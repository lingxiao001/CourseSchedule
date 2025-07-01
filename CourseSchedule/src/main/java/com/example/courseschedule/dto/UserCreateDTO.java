package com.example.courseschedule.dto;

import org.springframework.validation.annotation.Validated;

import com.example.courseschedule.entity.User.Role;

import lombok.Data;

@Data
@Validated
public class UserCreateDTO {
 
    private String username;
    
    private String password;
    
    private String realName;
    
    private Role role;
    
    // 学生相关字段
    private Long studentId;  // 学号
    private String grade;    // 年级
    private String className; // 班级
    
    // 教师相关字段
    private Long teacherId;  // 教师ID
    private String title;    // 职称
    private String department; // 部门
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public Long getStudentId() {
		return studentId;
	}
	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}
	public Long getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}   
    
}