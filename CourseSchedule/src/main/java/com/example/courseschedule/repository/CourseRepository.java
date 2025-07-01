package com.example.courseschedule.repository;

import com.example.courseschedule.entity.Course;
import com.example.courseschedule.entity.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 1. 基础查询方法
    List<Course> findByCredit(Double credit);
    List<Course> findByCourseNameContaining(String keyword);
    Optional<Course>findById(Long courseId);
    // 2. 查询已选某课程的所有学生信息（核心方法）
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN CourseSelection cs ON s.id = cs.student.id " +
           "JOIN TeachingClass tc ON cs.teachingClass.id = tc.id " +
           "WHERE tc.course.id = :courseId")
    List<Student> findStudentsEnrolledInCourse(@Param("courseId") Long courseId);

    // 3. 查询已选某课程的学生详细信息（带用户信息）
    @Query("SELECT s, u FROM Student s " +
           "JOIN s.user u " +
           "JOIN CourseSelection cs ON s.id = cs.student.id " +
           "JOIN TeachingClass tc ON cs.teachingClass.id = tc.id " +
           "WHERE tc.course.id = :courseId")
    List<Object[]> findStudentsWithUserInfoByCourse(@Param("courseId") Long courseId);

    // 4. 统计某课程的选课人数
    @Query("SELECT COUNT(DISTINCT cs.student.id) FROM CourseSelection cs " +
           "JOIN cs.teachingClass tc " +
           "WHERE tc.course.id = :courseId")
    Integer countStudentsEnrolled(@Param("courseId") Long courseId);

    // 5. 查询某课程的所有教学班及选课人数
    @Query("SELECT tc, COUNT(cs.student) FROM TeachingClass tc " +
           "LEFT JOIN CourseSelection cs ON tc.id = cs.teachingClass.id " +
           "WHERE tc.course.id = :courseId " +
           "GROUP BY tc.id")
    List<Object[]> findTeachingClassesWithEnrollmentCount(@Param("courseId") Long courseId);
}