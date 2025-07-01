package com.example.courseschedule.repository;

import com.example.courseschedule.entity.TeachingClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {

	boolean existsByCourseId(Long courseId);

    // 默认方法，根据课程ID查询班级
    @Query("SELECT tc FROM TeachingClass tc WHERE tc.course.id = :courseId")
    List<TeachingClass> findByCourseId(Long courseId);

    // 默认方法，根据教师ID查询班级
    List<TeachingClass> findByTeacherId(Long teacherId);

    // 自定义方法，根据班级代码查询班级
    @Query("SELECT tc FROM TeachingClass tc WHERE tc.classCode = :classCode")
    Optional<TeachingClass> findByClassCode(String classCode);
}
