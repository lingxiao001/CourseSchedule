package com.example.courseschedule.dto;

import com.example.courseschedule.entity.User;
import com.example.courseschedule.entity.User.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private Long userId;
    private String username;
    private String real_name;
    private Role role;
    private Long roleId;  // 学生ID或教师ID
    private String roleType; // "student" 或 "teacher"
    private Long studentId;
	private Long teacherId;

    public AuthResponseDTO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.real_name = user.getRealName();
        this.role = user.getRole();
        
        if (user.getRole() == Role.student && user.getStudent() != null) {
            this.roleId = user.getStudent().getId();
            this.roleType = "student";
            this.studentId = user.getStudent().getId(); // 新增：studentId 字段赋值
        } else if (user.getRole() == Role.teacher && user.getTeacher() != null) {
            this.roleId = user.getTeacher().getId();
            this.roleType = "teacher";
			this.teacherId = user.getTeacher().getId();
        }
    }

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealName() {
		return real_name;
	}

	public void setRealName(String real_name) {
		this.real_name = real_name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
}