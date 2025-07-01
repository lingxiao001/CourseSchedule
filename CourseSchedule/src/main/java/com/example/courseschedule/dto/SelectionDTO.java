package com.example.courseschedule.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import com.example.courseschedule.entity.TeachingClass;

@Data
@AllArgsConstructor
public class SelectionDTO {
    private Long id;
    private LocalDateTime selectionTime;

    private Long studentId;
    private Long teachingClassId;
	public SelectionDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getSelectionTime() {
		return selectionTime;
	}

	public void setSelectionTime(LocalDateTime selectionTime) {
		this.selectionTime = selectionTime;
	}

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Long getTeachingClassId() {
		return teachingClassId;
	}

	public void setTeachingClassId(Long teachingClassId) {
		this.teachingClassId = teachingClassId;
	}
}