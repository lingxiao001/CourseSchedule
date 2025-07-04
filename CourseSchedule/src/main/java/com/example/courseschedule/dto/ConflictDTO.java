package com.example.courseschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConflictDTO {
    private String type; // teacher, classroom, teachingClass
    private Long id;     // 相关实体ID
    private String message;
} 