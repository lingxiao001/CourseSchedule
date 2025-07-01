package com.example.courseschedule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.courseschedule.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
}