package com.example.courseschedule.repository;

import com.example.courseschedule.entity.Classroom;
import com.example.courseschedule.entity.TeachingClass;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IntelligentSchedulingRepository extends Repository<TeachingClass, Long> {
    
    // 查找满足条件的可用教室
    @Query("SELECT c FROM Classroom c " +
           "WHERE c.capacity >= (SELECT tc.maxStudents FROM TeachingClass tc WHERE tc.id = :teachingClassId) " +
           "AND c.id NOT IN (" +
           "   SELECT cs.classroom.id FROM ClassSchedule cs " +
           "   WHERE cs.dayOfWeek = :dayOfWeek " +
           "   AND ((cs.startTime < :endTime AND cs.endTime > :startTime))" +
           ")")
    List<Classroom> findAvailableClassrooms(
            @Param("teachingClassId") Long teachingClassId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
    
    // 查找教师可用时间段
    @Query("SELECT DISTINCT cs.dayOfWeek, cs.startTime, cs.endTime " +
           "FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.teacher.id = :teacherId " +
           "AND NOT EXISTS (" +
           "   SELECT 1 FROM ClassSchedule conflict " +
           "   WHERE conflict.teachingClass.teacher.id = :teacherId " +
           "   AND conflict.dayOfWeek = cs.dayOfWeek " +
           "   AND ((conflict.startTime < cs.endTime AND conflict.endTime > cs.startTime))" +
           ")")
    List<Object[]> findTeacherAvailableSlots(@Param("teacherId") Long teacherId);
    
    // 查找教室可用时间段
    @Query("SELECT DISTINCT cs.dayOfWeek, cs.startTime, cs.endTime " +
           "FROM ClassSchedule cs " +
           "WHERE cs.classroom.id = :classroomId " +
           "AND NOT EXISTS (" +
           "   SELECT 1 FROM ClassSchedule conflict " +
           "   WHERE conflict.classroom.id = :classroomId " +
           "   AND conflict.dayOfWeek = cs.dayOfWeek " +
           "   AND ((conflict.startTime < cs.endTime AND conflict.endTime > cs.startTime))" +
           ")")
    List<Object[]> findClassroomAvailableSlots(@Param("classroomId") Long classroomId);
}