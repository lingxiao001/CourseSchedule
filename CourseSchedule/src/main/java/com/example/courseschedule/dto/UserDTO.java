package com.example.courseschedule.dto;

import java.time.LocalDateTime;

import com.example.courseschedule.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String real_name;
    private User.Role role;
    private Long roleId; // StudentId or TeacherId based on role
    private LocalDateTime createdAt;

    // Student specific fields
    private String grade;
    private String className;
    
    // Teacher specific fields
    private String title;
    private String department;

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        dto.setRoleId(user.getRoleId());
        dto.setCreatedAt(user.getCreatedAt());
        
        if (user.getRole() == User.Role.student && user.getStudent() != null) {
            dto.setGrade(user.getStudent().getGrade());
            dto.setClassName(user.getStudent().getClassName());
        } else if (user.getRole() == User.Role.teacher && user.getTeacher() != null) {
            dto.setTitle(user.getTeacher().getTitle());
            dto.setDepartment(user.getTeacher().getDepartment());
        }
        
        return dto;
    }


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public void setRealName(String realName) {
		this.real_name = realName;
	}

	public User.Role getRole() {
		return role;
	}

	public void setRole(User.Role role) {
		this.role = role;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
    
}