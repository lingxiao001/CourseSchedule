package com.example.courseschedule.dto;

/**
 * 课程安排数据传输对象
 * 用于修复上课时间显示问题
 */
public class ClassScheduleDTO {
    private Integer dayOfWeek;
    private String startTime;
    private String endTime;
    private String classroomName;
    private String building;

    public ClassScheduleDTO(Integer dayOfWeek, String startTime, String endTime, 
                          String classroomName, String building) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroomName = classroomName;
        this.building = building;
    }

    // Getters and Setters
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * 获取星期几的中文显示
     */
    public String getDayOfWeekChinese() {
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "未知";
    }

    /**
     * 获取完整的上课时间字符串
     */
    public String getFullSchedule() {
        return getDayOfWeekChinese() + " " + startTime + "-" + endTime + " " + building + " " + classroomName;
    }
}