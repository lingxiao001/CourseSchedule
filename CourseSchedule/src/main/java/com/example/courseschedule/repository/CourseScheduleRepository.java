package com.example.courseschedule.repository;

import com.example.courseschedule.entity.ClassSchedule;
import com.example.courseschedule.entity.Classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    // 按星期和开始时间排序获取所有课程安排
    List<ClassSchedule> findAllByOrderByDayOfWeekAscStartTimeAsc();
    
    // 根据教学班ID查询课程安排
    List<ClassSchedule> findByTeachingClassId(Long teachingClassId);

    // 根据教学班ID列表查询课程安排
    List<ClassSchedule> findByTeachingClassIdIn(List<Long> teachingClassIds);

    // 根据教室ID查询课程安排
    List<ClassSchedule> findByClassroomId(Long classroomId);

    // 查询指定教师的所有课程安排
    @Query("SELECT cs FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.teacher.id = :teacherId " +
           "ORDER BY cs.dayOfWeek, cs.startTime")
    List<ClassSchedule> findByTeacherId(@Param("teacherId") Long teacherId);

    // 检查同一教学班的时间冲突
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.id = :teachingClassId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
    boolean existsTimeConflict(
            @Param("teachingClassId") Long teachingClassId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // 检查教学班时间冲突（排除自身）
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.id = :teachingClassId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime)) " +
           "AND cs.id != :excludeId")
    boolean existsTimeConflictExcludingSelf(
            @Param("teachingClassId") Long teachingClassId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Long excludeId);

    // 检查教师时间冲突
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.teacher.id = :teacherId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
    boolean existsTeacherTimeConflict(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // 检查教师时间冲突（排除自身）
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.teachingClass.teacher.id = :teacherId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime)) " +
           "AND cs.id != :excludeId")
    boolean existsTeacherTimeConflictExcludingSelf(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Long excludeId);

    // 检查教室时间冲突
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.classroom.id = :classroomId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
    boolean existsClassroomTimeConflict(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // 检查教室时间冲突（排除自身）
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs " +
           "WHERE cs.classroom.id = :classroomId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime)) " +
           "AND cs.id != :excludeId")
    boolean existsClassroomTimeConflictExcludingSelf(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Long excludeId);

    // 查询指定星期和时间的可用教室
    @Query("SELECT cs.classroom FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
    List<Classroom> findOccupiedClassrooms(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // 查询指定时间段内的课程安排
    @Query("SELECT cs FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime)) " +
           "ORDER BY cs.startTime")
    List<ClassSchedule> findSchedulesInTimeRange(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

	//新增
	@Query("SELECT cs FROM ClassSchedule cs " +
		       "WHERE cs.dayOfWeek = :dayOfWeek " +
		       "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
		List<ClassSchedule> findByDayOfWeekAndTimeConflict(
		    @Param("dayOfWeek") Integer dayOfWeek,
		    @Param("startTime") String startTime,
		    @Param("endTime") String endTime);
}