package com.example.courseschedule.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username="未知";
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String realName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)


    private Role role;
    public enum Role {
        student, teacher, admin
    }
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
	@OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
    private Student student;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    private Teacher teacher;
    public Student getStudent() {
        return student;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public long getRoleId() {
        if (this.role == null) return -1;
        
        return switch (this.role) {
            case student -> this.getStudent().getId(); // 如果是学生，返回学生的ID
            case teacher -> this.getTeacher().getId(); // 如果是老师，返回老师的ID
            case admin -> -1; // 管理员没有对应的实体ID，返回-1
            default -> -1; // 其他情况返回-1
        };
    }

    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
	    return this.role; // 直接返回枚举值
	}
	public void setRole(Role role) {
	    if (role == null) {
	        throw new IllegalArgumentException("Role cannot be null");
	    }
	    this.role = role;
	}
    
}