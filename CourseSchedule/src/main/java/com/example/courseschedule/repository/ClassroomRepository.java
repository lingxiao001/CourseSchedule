package com.example.courseschedule.repository;

import com.example.courseschedule.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByBuildingAndClassroomName(String building, String classroomName);

    @Query("SELECT c FROM Classroom c WHERE c.id NOT IN (" +
           "SELECT cs.classroom.id FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))) " +
           "ORDER BY c.building, c.classroomName")
    Optional<Classroom> findFirstAvailableClassroom(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Query("SELECT c FROM Classroom c WHERE c.building = :building " +
           "AND c.id NOT IN (" +
           "SELECT cs.classroom.id FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))) " +
           "ORDER BY c.classroomName")
    Optional<Classroom> findFirstAvailableClassroomInBuilding(
            @Param("building") String building,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Query("SELECT c FROM Classroom c WHERE c.id NOT IN (" +
           "SELECT cs.classroom.id FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))) " +
           "ORDER BY c.building, c.classroomName")
    List<Classroom> findAvailableClassrooms(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Query("SELECT c FROM Classroom c WHERE c.building = :building " +
           "AND c.id NOT IN (" +
           "SELECT cs.classroom.id FROM ClassSchedule cs " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND ((cs.startTime < :endTime AND cs.endTime > :startTime))) " +
           "ORDER BY c.classroomName")
    List<Classroom> findAvailableClassroomsInBuilding(
            @Param("building") String building,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

	List<Classroom> findByBuilding(String building);
}
