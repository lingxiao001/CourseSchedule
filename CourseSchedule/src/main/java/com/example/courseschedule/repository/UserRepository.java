package com.example.courseschedule.repository;

import com.example.courseschedule.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

//UserRepository.java 新增
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"student"})
    Optional<User> findWithStudentById(Long userId);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<User> findWithTeacherById(Long userId);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(u.realName) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<User> searchUsers(@Param("term") String term, Pageable pageable);
}