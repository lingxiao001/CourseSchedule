package com.example.courseschedule.service;

import com.example.courseschedule.entity.*;
import com.example.courseschedule.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BasicIntelligentSchedulingService {
    private final TeachingClassRepository teachingClassRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseScheduleRepository scheduleRepository;

    public BasicIntelligentSchedulingService(
            TeachingClassRepository teachingClassRepository,
            ClassroomRepository classroomRepository,
            CourseScheduleRepository scheduleRepository) {
        this.teachingClassRepository = teachingClassRepository;
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * 遗传算法排课主入口
     * @return 所有排课结果
     */
    public List<ClassSchedule> autoSchedule() {
        // 1. 获取所有教学班、教室、时间段
        List<TeachingClass> teachingClasses = teachingClassRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<String[]> timeSlots = getTimeSlots();

        // 2. 遗传算法参数
        int populationSize = 50;
        int generations = 100;
        double crossoverRate = 0.8;
        double mutationRate = 0.1;

        // 3. 初始化种群
        List<ScheduleChromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(ScheduleChromosome.randomChromosome(teachingClasses, classrooms, timeSlots));
        }

        // 4. 进化
        ScheduleChromosome best = null;
        for (int gen = 0; gen < generations; gen++) {
            // 计算适应度
            for (ScheduleChromosome c : population) {
                c.calculateFitness();
            }
            // 选择
            List<ScheduleChromosome> newPopulation = new ArrayList<>();
            while (newPopulation.size() < populationSize) {
                ScheduleChromosome p1 = tournamentSelect(population);
                ScheduleChromosome p2 = tournamentSelect(population);
                // 交叉
                if (Math.random() < crossoverRate) {
                    List<ScheduleChromosome> children = p1.crossover(p2);
                    newPopulation.addAll(children);
                } else {
                    newPopulation.add(p1.copy());
                    newPopulation.add(p2.copy());
                }
            }
            // 变异
            for (ScheduleChromosome c : newPopulation) {
                if (Math.random() < mutationRate) {
                    c.mutate(classrooms, timeSlots);
                }
            }
            // 保留最优
            population = newPopulation.stream().limit(populationSize).collect(Collectors.toList());
            best = Collections.max(population, Comparator.comparingDouble(c -> c.fitness));
        }

        // 5. 保存最优解到数据库
        scheduleRepository.deleteAll(); // 清空原有排课
        List<ClassSchedule> result = best.toSchedules();
        scheduleRepository.saveAll(result);
        return result;
    }

    /**
     * 单个教学班智能排课（遗传算法）
     * @param teachingClassId 教学班ID
     * @return 该班级的排课结果
     */
    public List<ClassSchedule> autoScheduleForTeachingClass(Long teachingClassId) {
        TeachingClass teachingClass = teachingClassRepository.findById(teachingClassId)
                .orElseThrow(() -> new RuntimeException("教学班不存在"));
        List<Classroom> classrooms = classroomRepository.findAll();
        List<String[]> timeSlots = getTimeSlots();
        int hours = teachingClass.getCourse().getHours();

        // 遗传算法参数
        int populationSize = 30;
        int generations = 60;
        double crossoverRate = 0.8;
        double mutationRate = 0.1;

        // 初始化种群
        List<ScheduleChromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(ScheduleChromosome.randomChromosomeSingle(teachingClass, hours, classrooms, timeSlots));
        }

        // 进化
        ScheduleChromosome best = null;
        for (int gen = 0; gen < generations; gen++) {
            for (ScheduleChromosome c : population) {
                c.calculateFitness();
            }
            List<ScheduleChromosome> newPopulation = new ArrayList<>();
            while (newPopulation.size() < populationSize) {
                ScheduleChromosome p1 = tournamentSelect(population);
                ScheduleChromosome p2 = tournamentSelect(population);
                if (Math.random() < crossoverRate) {
                    List<ScheduleChromosome> children = p1.crossover(p2);
                    newPopulation.addAll(children);
                } else {
                    newPopulation.add(p1.copy());
                    newPopulation.add(p2.copy());
                }
            }
            for (ScheduleChromosome c : newPopulation) {
                if (Math.random() < mutationRate) {
                    c.mutate(classrooms, timeSlots);
                }
            }
            population = newPopulation.stream().limit(populationSize).collect(Collectors.toList());
            best = Collections.max(population, Comparator.comparingDouble(c -> c.fitness));
        }

        // 只删除该班级原有排课
        List<ClassSchedule> old = scheduleRepository.findByTeachingClassId(teachingClassId);
        scheduleRepository.deleteAll(old);
        List<ClassSchedule> result = best.toSchedules();
        scheduleRepository.saveAll(result);
        return result;
    }

    // 染色体类
    static class ScheduleChromosome {
        // 基因：每个教学班的排课安排
        static class Gene {
            TeachingClass teachingClass;
            int dayOfWeek; // 1-7
            String startTime;
            String endTime;
            Classroom classroom;
        }
        List<Gene> genes;
        double fitness;

        // 随机生成一个染色体
        static ScheduleChromosome randomChromosome(List<TeachingClass> teachingClasses, List<Classroom> classrooms, List<String[]> timeSlots) {
            ScheduleChromosome c = new ScheduleChromosome();
            c.genes = new ArrayList<>();
            Random rand = new Random();
            for (TeachingClass tc : teachingClasses) {
                int hours = tc.getCourse().getHours();
                int maxLessons = (hours > 32) ? 2 : 1;
                for (int i = 0; i < maxLessons; i++) {
                    Gene g = new Gene();
                    g.teachingClass = tc;
                    int slotIdx = rand.nextInt(timeSlots.size());
                    String[] slot = timeSlots.get(slotIdx);
                    g.dayOfWeek = rand.nextInt(7) + 1;
                    g.startTime = slot[0];
                    g.endTime = slot[1];
                    g.classroom = classrooms.get(rand.nextInt(classrooms.size()));
                    c.genes.add(g);
                }
            }
            return c;
        }

        // 单班级随机染色体
        static ScheduleChromosome randomChromosomeSingle(TeachingClass tc, int hours, List<Classroom> classrooms, List<String[]> timeSlots) {
            ScheduleChromosome c = new ScheduleChromosome();
            c.genes = new ArrayList<>();
            Random rand = new Random();
            int maxLessons = (hours > 32) ? 2 : 1;
            for (int i = 0; i < maxLessons; i++) {
                Gene g = new Gene();
                g.teachingClass = tc;
                int slotIdx = rand.nextInt(timeSlots.size());
                String[] slot = timeSlots.get(slotIdx);
                g.dayOfWeek = rand.nextInt(7) + 1;
                g.startTime = slot[0];
                g.endTime = slot[1];
                g.classroom = classrooms.get(rand.nextInt(classrooms.size()));
                c.genes.add(g);
            }
            return c;
        }

        // 适应度计算
        void calculateFitness() {
            double score = 0;
            Map<Long, Map<Integer, Integer>> classDayCount = new HashMap<>(); // teachingClassId -> (dayOfWeek -> count)
            // 1. 无冲突（教师、教室、班级）+ 统计每班每天排课数
            for (int i = 0; i < genes.size(); i++) {
                Gene g1 = genes.get(i);
                // 统计每天排课数
                classDayCount.computeIfAbsent(g1.teachingClass.getId(), k -> new HashMap<>());
                Map<Integer, Integer> dayMap = classDayCount.get(g1.teachingClass.getId());
                dayMap.put(g1.dayOfWeek, dayMap.getOrDefault(g1.dayOfWeek, 0) + 1);
                for (int j = i + 1; j < genes.size(); j++) {
                    Gene g2 = genes.get(j);
                    if (g1.dayOfWeek == g2.dayOfWeek && timeOverlap(g1, g2)) {
                        // 教师冲突
                        if (g1.teachingClass.getTeacher().getId().equals(g2.teachingClass.getTeacher().getId())) {
                            score -= 1000;
                        }
                        // 教室冲突
                        if (g1.classroom.getId().equals(g2.classroom.getId())) {
                            score -= 1000;
                        }
                        // 教学班冲突
                        if (g1.teachingClass.getId().equals(g2.teachingClass.getId())) {
                            score -= 1000;
                        }
                    }
                }
                // 2. 教室容量不足
                if (g1.classroom.getCapacity() < g1.teachingClass.getMaxStudents()) {
                    score -= 500;
                }
                // 3. 时间优先级加分
                switch (g1.startTime) {
                    case "08:00":
                    case "10:00":
                        score += 20; // 上午
                        break;
                    case "13:30":
                    case "15:30":
                        score += 10; // 下午
                        break;
                    case "18:00":
                        score -= 5; // 晚上
                        break;
                }
            }
            // 4. 每班每天不超过1节
            for (Map.Entry<Long, Map<Integer, Integer>> entry : classDayCount.entrySet()) {
                Map<Integer, Integer> dayMap = entry.getValue();
                for (int cnt : dayMap.values()) {
                    if (cnt > 1) score -= 800 * (cnt - 1); // 同一天多节严重扣分
                }
            }
            // 5. 分散性奖励：同班排课之间天数和时间段间隔越大越好
            Map<Long, List<Gene>> classGenes = new HashMap<>();
            for (Gene g : genes) {
                classGenes.computeIfAbsent(g.teachingClass.getId(), k -> new ArrayList<>()).add(g);
            }
            for (List<Gene> geneList : classGenes.values()) {
                if (geneList.size() > 1) {
                    double dayGapSum = 0;
                    double timeGapSum = 0;
                    for (int i = 0; i < geneList.size(); i++) {
                        for (int j = i + 1; j < geneList.size(); j++) {
                            int dayGap = Math.abs(geneList.get(i).dayOfWeek - geneList.get(j).dayOfWeek);
                            int timeGap = Math.abs(timeSlotIndex(geneList.get(i).startTime) - timeSlotIndex(geneList.get(j).startTime));
                            dayGapSum += dayGap;
                            timeGapSum += timeGap;
                        }
                    }
                    // 间隔越大越好，奖励分散性
                    score += (dayGapSum * 30 + timeGapSum * 10);
                }
            }
            this.fitness = score;
        }

        // 判断时间段是否重叠
        static boolean timeOverlap(Gene g1, Gene g2) {
            return g1.startTime.compareTo(g2.endTime) < 0 && g2.startTime.compareTo(g1.endTime) < 0;
        }

        // 交叉
        List<ScheduleChromosome> crossover(ScheduleChromosome other) {
            Random rand = new Random();
            int point = rand.nextInt(genes.size());
            ScheduleChromosome c1 = this.copy();
            ScheduleChromosome c2 = other.copy();
            for (int i = point; i < genes.size(); i++) {
                Gene tmp = c1.genes.get(i);
                c1.genes.set(i, c2.genes.get(i));
                c2.genes.set(i, tmp);
            }
            return List.of(c1, c2);
        }

        // 变异
        void mutate(List<Classroom> classrooms, List<String[]> timeSlots) {
            Random rand = new Random();
            int idx = rand.nextInt(genes.size());
            Gene g = genes.get(idx);
            g.dayOfWeek = rand.nextInt(7) + 1;
            String[] slot = timeSlots.get(rand.nextInt(timeSlots.size()));
            g.startTime = slot[0];
            g.endTime = slot[1];
            g.classroom = classrooms.get(rand.nextInt(classrooms.size()));
        }

        // 深拷贝
        ScheduleChromosome copy() {
            ScheduleChromosome c = new ScheduleChromosome();
            c.genes = new ArrayList<>();
            for (Gene g : this.genes) {
                Gene ng = new Gene();
                ng.teachingClass = g.teachingClass;
                ng.dayOfWeek = g.dayOfWeek;
                ng.startTime = g.startTime;
                ng.endTime = g.endTime;
                ng.classroom = g.classroom;
                c.genes.add(ng);
            }
            c.fitness = this.fitness;
            return c;
        }

        // 转为数据库实体
        List<ClassSchedule> toSchedules() {
            List<ClassSchedule> list = new ArrayList<>();
            for (Gene g : genes) {
                ClassSchedule cs = new ClassSchedule();
                cs.setTeachingClass(g.teachingClass);
                cs.setDayOfWeek(g.dayOfWeek);
                cs.setStartTime(g.startTime);
                cs.setEndTime(g.endTime);
                cs.setClassroom(g.classroom);
                list.add(cs);
            }
            return list;
        }

        // 获取时间段在timeSlots中的索引
        static int timeSlotIndex(String startTime) {
            switch (startTime) {
                case "08:00": return 0;
                case "10:00": return 1;
                case "13:30": return 2;
                case "15:30": return 3;
                case "18:00": return 4;
                default: return -1;
            }
        }
    }

    // 锦标赛选择
    private ScheduleChromosome tournamentSelect(List<ScheduleChromosome> population) {
        Random rand = new Random();
        int k = 3;
        ScheduleChromosome best = null;
        for (int i = 0; i < k; i++) {
            ScheduleChromosome c = population.get(rand.nextInt(population.size()));
            if (best == null || c.fitness > best.fitness) {
                best = c;
            }
        }
        return best;
    }

    // 获取所有可用时间段
    private List<String[]> getTimeSlots() {
        return List.of(
                new String[]{"08:00", "09:30"},
                new String[]{"10:00", "11:30"},
                new String[]{"13:30", "15:00"},
                new String[]{"15:30", "17:00"},
                new String[]{"18:00", "19:30"}
        );
    }
}