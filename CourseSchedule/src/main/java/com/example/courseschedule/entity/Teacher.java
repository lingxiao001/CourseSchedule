package com.example.courseschedule.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teacher")  // 对应数据库中的teachers表
public class Teacher {
    @Id
    private Long id;

    // 关联到用户表（如果存在）
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;      // 职称
    @Column(nullable = false)
    private String department; // 所属院系
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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