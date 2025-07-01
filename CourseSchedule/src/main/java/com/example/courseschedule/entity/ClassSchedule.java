package com.example.courseschedule.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "class_schedule")
public class ClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer dayOfWeek; // 1-7表示周一到周日
    
    @Column(nullable = false)
    private String startTime;
    
    @Column(nullable = false)
    private String endTime;
    
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "building", referencedColumnName = "building"),
        @JoinColumn(name = "classroomName", referencedColumnName = "classroomName")
    })
    
    
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    @ManyToOne
    @JoinColumn(name = "teaching_class_id", nullable = false)
    @JsonIgnore
    private TeachingClass teachingClass;

    // 构造函数、getter和setter
    
    public ClassSchedule(Integer dayOfWeek, String startTime, String endTime, 
            Classroom classroom, TeachingClass teachingClass) {
    	this.dayOfWeek = dayOfWeek;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.classroom = classroom;
    	this.teachingClass = teachingClass;
    }

    public ClassSchedule() {
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public TeachingClass getTeachingClass() {
        return teachingClass;
    }

    public void setTeachingClass(TeachingClass teachingClass) {
        this.teachingClass = teachingClass;
    }


}