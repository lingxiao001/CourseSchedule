package com.example.courseschedule.service;
import com.example.courseschedule.controller.CourseController;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.*;
import com.example.courseschedule.utils.XfyunApiClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BasicIntelligentSchedulingService {
    private final IntelligentSchedulingRepository intelligentSchedulingRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseScheduleRepository scheduleRepository;
    
    // 排课模式枚举
    public enum SchedulingMode {
        RULE_BASED,  // 基于规则
        AI_BASED     // 基于大模型
    }
    public BasicIntelligentSchedulingService(
            IntelligentSchedulingRepository intelligentSchedulingRepository,
            TeachingClassRepository teachingClassRepository,
            ClassroomRepository classroomRepository,
            CourseScheduleRepository scheduleRepository) {
        this.intelligentSchedulingRepository = intelligentSchedulingRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
    }
    
    /**
     * 自动排课 - 支持两种模式
     */
    public ClassSchedule autoSchedule(Long teachingClassId, SchedulingMode mode) {
        return mode == SchedulingMode.AI_BASED ? 
               aiBasedSchedule(teachingClassId) : 
               ruleBasedSchedule(teachingClassId);
    }
    
    /**
     * 基于规则的排课
     */
    public ClassSchedule ruleBasedSchedule(Long teachingClassId) {
        TeachingClass teachingClass = getTeachingClass(teachingClassId);
        System.out.println("=== 开始基于规则的排课 ===");
        System.out.printf("教学班ID: %d, 课程: %s, 教师: %s, 学生数: %d/%d\n",
                teachingClassId,
                teachingClass.getCourse().getCourseName(),
                teachingClass.getTeacher().getUser().getRealName(),
                teachingClass.getCurrentStudents(),
                teachingClass.getMaxStudents());

        for (int day = 1; day <= 7; day++) {
            System.out.printf("\n=== 检查星期 %d ===\n", day);
            
            for (String timeSlot : getTimeSlots()) {
                String[] times = timeSlot.split("-");
                String startTime = times[0];
                String endTime = times[1];
                
                System.out.printf("\n检查时间段: %s-%s\n", startTime, endTime);
                
                // 打印查询参数
                System.out.printf("查询参数 - 所需容量: %d, 星期: %d, 开始时间: %s, 结束时间: %s\n",
                        teachingClass.getMaxStudents(), day, startTime, endTime);
                
                List<Classroom> availableClassrooms = findAvailableClassrooms(
                    teachingClassId, day, startTime, endTime);
                
                System.out.printf("找到 %d 个可用教室\n", availableClassrooms.size());
                if (!availableClassrooms.isEmpty()) {
                    System.out.println("可用教室列表:");
                    availableClassrooms.forEach(c -> System.out.printf(
                        "- %s-%s (容量: %d)\n", 
                        c.getBuilding(), 
                        c.getClassroomName(), 
                        c.getCapacity()));
                    
                    boolean teacherAvailable = !hasTeacherConflict(teachingClass, day, startTime, endTime);
                    System.out.printf("教师 %s 在该时间段%s\n",
                        teachingClass.getTeacher().getUser().getRealName(),
                        teacherAvailable ? "可用" : "有冲突");
                    
                    if (teacherAvailable) {
                        Classroom selectedClassroom = availableClassrooms.get(0);
                        System.out.printf("\n=== 找到合适排课方案 ===\n");
                        System.out.printf("教室: %s-%s\n", selectedClassroom.getBuilding(), selectedClassroom.getClassroomName());
                        System.out.printf("时间: 周%d %s-%s\n", day, startTime, endTime);
                        
                        ClassSchedule schedule = createSchedule(
                            teachingClass, 
                            selectedClassroom, 
                            day, startTime, endTime);
                        
                        System.out.println("排课成功!");
                        return schedule;
                    }
                } else {
                    // 打印所有教室信息帮助调试
                    List<Classroom> allClassrooms = classroomRepository.findAll();
                    System.out.println("所有教室信息:");
                    allClassrooms.forEach(c -> System.out.printf(
                        "- %s-%s (容量: %d)\n", 
                        c.getBuilding(), 
                        c.getClassroomName(), 
                        c.getCapacity()));
                    
                    // 检查时间冲突
                    List<ClassSchedule> conflictingSchedules = scheduleRepository.findByDayOfWeekAndTimeConflict(
                        day, startTime, endTime);
                    System.out.printf("冲突的排课记录(%d条):\n", conflictingSchedules.size());
                    conflictingSchedules.forEach(s -> System.out.printf(
                        "- 周%d %s-%s: %s (%s-%s)\n",
                        s.getDayOfWeek(), s.getStartTime(), s.getEndTime(),
                        s.getTeachingClass().getCourse().getCourseName(),
                        s.getClassroom().getBuilding(),
                        s.getClassroom().getClassroomName()));
                }
            }
        }
        
        System.err.println("!!! 无法找到合适的排课时间 !!!");
        throw new RuntimeException("无法找到合适的排课时间");
    }
    
    /**
     * 基于大模型的智能排课
     */
    public ClassSchedule aiBasedSchedule(Long teachingClassId) {
        TeachingClass teachingClass = getTeachingClass(teachingClassId);
        List<Classroom> classrooms = classroomRepository.findAll();
        List<ClassSchedule> existingSchedules = scheduleRepository.findAll();
        
        try {
            // 1. 构建大模型提示词
            String prompt = buildAIPrompt(teachingClass, classrooms, existingSchedules);
            
            // 2. 调用大模型API
            String response = XfyunApiClient.callXfyunAPI(prompt);
            
            // 3. 解析并创建排课
            return parseAIResponse(response, teachingClass);
        } catch (Exception e) {
            // 大模型失败时回退到基于规则的排课
            return ruleBasedSchedule(teachingClassId);
        }
    }
    
    /**
     * 构建大模型提示词
     */
    public String buildAIPrompt(TeachingClass teachingClass,
                               List<Classroom> classrooms,
                               List<ClassSchedule> existingSchedules) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能排课系统，请根据以下信息生成最优排课方案。\n\n");
        
        // 教学班信息
        prompt.append("### 教学班信息\n");
        prompt.append("- 课程: ").append(teachingClass.getCourse().getCourseName()).append("\n");
        prompt.append("- 教师: ").append(teachingClass.getTeacher().getUser().getRealName()).append("\n");
        prompt.append("- 学生数: ").append(teachingClass.getCurrentStudents()).append("\n");
        prompt.append("- 周课时: ").append(teachingClass.getCourse().getHours()).append("\n\n");
        
        // 教室信息
        prompt.append("### 可用教室\n");
        classrooms.forEach(c -> prompt.append("- ")
            .append(c.getBuilding()).append("-").append(c.getClassroomName())
            .append(" (容量: ").append("100").append(")\n"));
        
        prompt.append("\n");

        // 现有排课
        prompt.append("### 现有课程安排\n");
        existingSchedules.forEach(s -> prompt.append("- 周")
            .append(s.getDayOfWeek()).append(" ")
            .append(s.getStartTime()).append("-").append(s.getEndTime()).append(": ")
            .append(s.getTeachingClass().getCourse().getCourseName()).append(" (")
            .append(s.getClassroom().getBuilding()).append("-")
            .append(s.getClassroom().getClassroomName()).append(")\n"));
        prompt.append("\n");
        
        // 排课规则
        prompt.append("### 排课规则\n");
        prompt.append("1. 教室容量 >= 学生人数\n");
        prompt.append("2. 避免教师时间冲突\n");
        prompt.append("3. 避免教室时间冲突\n");
        prompt.append("4. 优先上午时段(08:00-11:20)\n");
        prompt.append("5. 同一课程间隔合理分布\n\n");
        
        // 响应格式
        prompt.append("请返回JSON格式的排课方案，包含以下字段:\n");
        prompt.append("{\n");
        prompt.append("  \"building\": \"教学楼名\",\n");
        prompt.append("  \"classroom\": \"教室号\",\n");
        prompt.append("  \"day\": 1-7, // 星期几\n");
        prompt.append("  \"start\": \"HH:mm\",\n");
        prompt.append("  \"end\": \"HH:mm\",\n");
        prompt.append("  \"reason\": \"选择理由\"\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * 解析大模型响应
     */
    private ClassSchedule parseAIResponse(String response, TeachingClass teachingClass) {
        JSONObject json = JSON.parseObject(response);
        JSONObject content = json.getJSONObject("payload")
                               .getJSONObject("choices")
                               .getJSONArray("text")
                               .getJSONObject(0)
                               .getJSONObject("content");
        
        // 获取教室
        Classroom classroom = classroomRepository.findByBuildingAndClassroomName(
            content.getString("building"), 
            content.getString("classroom"))
            .orElseThrow(() -> new RuntimeException("大模型返回的教室不存在"));
        
        // 创建排课
        return createSchedule(
            teachingClass,
            classroom,
            content.getInteger("day"),
            content.getString("start"),
            content.getString("end"));
    }
    
    // ========== 以下为辅助方法 ==========
    
    private TeachingClass getTeachingClass(Long teachingClassId) {
        return teachingClassRepository.findById(teachingClassId)
            .orElseThrow(() -> new RuntimeException("教学班不存在"));
    }
    
    private List<Classroom> findAvailableClassrooms(Long teachingClassId, 
                                                  int day, 
                                                  String startTime, 
                                                  String endTime) {
        return intelligentSchedulingRepository
            .findAvailableClassrooms(teachingClassId, day, startTime, endTime);
    }
    
    private boolean hasTeacherConflict(TeachingClass teachingClass, 
                                     int day, 
                                     String startTime, 
                                     String endTime) {
        return scheduleRepository.existsTeacherTimeConflict(
            teachingClass.getTeacher().getId(), day, startTime, endTime);
    }
    
    private ClassSchedule createSchedule(TeachingClass teachingClass,
                                       Classroom classroom,
                                       int day,
                                       String startTime,
                                       String endTime) {
        ClassSchedule schedule = new ClassSchedule();
        schedule.setDayOfWeek(day);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setClassroom(classroom);
        schedule.setTeachingClass(teachingClass);
        return scheduleRepository.save(schedule);
    }
    
    private List<String> getTimeSlots() {
        return List.of(
            "08:00-09:30", "09:50-11:20",
            "13:30-15:00", "15:20-16:50",
            "18:30-20:00"
        );
    }
    
    private Map<String, Object> toMap(JSONObject json) {
        return json.getInnerMap();
    }
    
    /**
     * 查找教学班所有可能的排课时间段
     */
    public List<ClassSchedule> findPossibleSchedules(Long teachingClassId) {
        TeachingClass teachingClass = getTeachingClass(teachingClassId);
        
        return intelligentSchedulingRepository
            .findTeacherAvailableSlots(teachingClass.getTeacher().getId())
            .stream()
            .map(slot -> {
                ClassSchedule schedule = new ClassSchedule();
                schedule.setDayOfWeek((Integer) slot[0]);
                schedule.setStartTime((String) slot[1]);
                schedule.setEndTime((String) slot[2]);
                schedule.setTeachingClass(teachingClass);
                return schedule;
            })
            .collect(Collectors.toList());
    }
}