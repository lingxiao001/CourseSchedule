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
    
    // Additional fields for student list display
    private String studentName;
    private String courseName;
    private String teacherName;
    
	public SelectionDTO() {
	}
}