package com.example.courseschedule.dto;

import com.example.courseschedule.entity.User.Role;
import lombok.Data;

@Data
public class UserUpdateDTO {
    
	private String real_name;
    
    private Role role;
    // 学生相关字段
    private Long studentId;  // 学号
    private String grade;    // 年级
    private String className; // 班级
    
    // 教师相关字段
    private Long teacherId;  // 教师ID
    private String title;    // 职称
    private String department; // 部门
    
    private String newPassword;

	public String getRealName() {
		return real_name;
	}

	public void setRealName(String realName) {
		this.real_name = realName;
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

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
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