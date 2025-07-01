package com.example.courseschedule.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_selection", 
uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "teaching_class_id"}))
public class CourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "teaching_class_id",nullable = false)
    private TeachingClass teachingClass;

    
    private LocalDateTime selectionTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public TeachingClass getTeachingClass() {
		return teachingClass;
	}

	public void setTeachingClass(TeachingClass teachingClass) {
		this.teachingClass = teachingClass;
	}

	public LocalDateTime getSelectionTime() {
		return selectionTime;
	}

	public void setSelectionTime(LocalDateTime selectionTime) {
		this.selectionTime = selectionTime;
	}
    
}

