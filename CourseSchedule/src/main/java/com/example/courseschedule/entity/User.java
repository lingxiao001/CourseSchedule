package com.example.courseschedule.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username = "未知";
    
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
    
    
    // Lombok generates getters/setters; keep getRoleId(), onCreate(), onUpdate(), getStudent/getTeacher

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
	}

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    // 关联实体
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Student student;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Teacher teacher;
    
}