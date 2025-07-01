package com.example.courseschedule.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "classroom")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String building;
    
    @Column(nullable = false)
    private String classroomName;

    @Column(nullable = false)
    private int capacity; // 新增容量字段
    
    @Override
    public String toString() {
        return building + "-" + classroomName;
    }
    public String getFullName() {
        return building + "-" + classroomName;
    }
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
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
    
}