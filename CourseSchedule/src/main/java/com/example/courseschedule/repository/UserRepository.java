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

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.realName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByUsernameContainingOrRealNameContaining(
            @Param("searchTerm") String username,
            @Param("searchTerm") String realName,
            Pageable pageable);
}