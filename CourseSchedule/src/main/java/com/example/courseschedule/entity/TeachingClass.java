package com.example.courseschedule.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teaching_class")
public class TeachingClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String classCode;
    private Integer maxStudents;
    private Integer currentStudents;
    
    @OneToMany(mappedBy = "teachingClass", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CourseSelection> courseSelections;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClassCode() {
		return classCode;
	}

	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}

	public Integer getMaxStudents() {
		return maxStudents;
	}

	public void setMaxStudents(Integer maxStudents) {
		this.maxStudents = maxStudents;
	}

	public Integer getCurrentStudents() {
		return currentStudents;
	}

	public void setCurrentStudents(Integer currentStudents) {
		this.currentStudents = currentStudents;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public List<ClassSchedule> getClassSchedules() {
		return classSchedules;
	}

	public void setClassSchedules(List<ClassSchedule> classSchedules) {
		this.classSchedules = classSchedules;
	}

	@ManyToOne
    @JoinColumn(name = "course_id",nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy = "teachingClass",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ClassSchedule> classSchedules;
}