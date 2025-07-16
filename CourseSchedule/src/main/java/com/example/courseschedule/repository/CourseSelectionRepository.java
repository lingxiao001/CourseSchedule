package com.example.courseschedule.repository;

import com.example.courseschedule.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
	boolean existsByTeachingClassId(Long teachingClassId);
    // 1. 根据学生ID查询选课记录（基础版）  
	    List<CourseSelection> findByStudentId(Long studentId);
    
		CourseSelection findByStudentIdAndTeachingClassId(Long studentId, Long teachingClassId);
    int countByTeachingClassId(Long teachingClassId);

    // 带详细信息的查询
    @Query("SELECT cs FROM CourseSelection cs " +
           "JOIN FETCH cs.student s " +
           "JOIN FETCH cs.teachingClass tc " +
           "JOIN FETCH tc.course " +
           "JOIN FETCH tc.teacher " +
           "ORDER BY cs.selectionTime DESC")
    List<CourseSelection> findAllWithDetails();

    @Query("SELECT cs FROM CourseSelection cs " +
           "JOIN FETCH cs.student s " +
           "JOIN FETCH cs.teachingClass tc " +
           "LEFT JOIN FETCH tc.classSchedules sc " +
           "JOIN FETCH tc.course " +
           "JOIN FETCH tc.teacher " +
           "WHERE s.id = :studentId " +
           "ORDER BY cs.selectionTime DESC")
    List<CourseSelection> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("SELECT cs FROM CourseSelection cs " +
           "JOIN FETCH cs.student s " +
           "JOIN FETCH cs.teachingClass tc " +
           "JOIN FETCH tc.course " +
           "JOIN FETCH tc.teacher " +
           "WHERE tc.id = :teachingClassId " +
           "ORDER BY cs.selectionTime DESC")
    List<CourseSelection> findByTeachingClassIdWithDetails(@Param("teachingClassId") Long teachingClassId);



    // 3. 检查是否已选过某课程


    // 5. 查询冲突课程（同一时间段）
    @Query("SELECT cs FROM CourseSelection cs " +
           "JOIN cs.teachingClass tc " +
           "JOIN tc.classSchedules sch " +
           "WHERE cs.student.id = :studentId " +
           "AND sch.dayOfWeek = :dayOfWeek " +
           "AND ((sch.startTime <= :endTime AND sch.endTime >= :startTime))")
    List<CourseSelection> findConflictingSelections(
            @Param("studentId") Long studentId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
    
    // 根据教学班ID删除选课记录
    @Modifying
    @Query("DELETE FROM CourseSelection cs WHERE cs.teachingClass.id = :teachingClassId")
    void deleteByTeachingClassId(@Param("teachingClassId") Long teachingClassId);
    
    // 根据学生ID删除选课记录
    @Modifying
    @Query("DELETE FROM CourseSelection cs WHERE cs.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}