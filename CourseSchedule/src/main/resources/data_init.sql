-- --------------------------------------------------
-- 课程排课系统 初始化数据脚本
-- 执行顺序按照外键依赖关系安排
    -- --------------------------------------------------

-- 1. 用户表 ------------------------------------------------
INSERT INTO user (id, username, password, real_name, role, created_at, updated_at) VALUES
  (1, 'admin', 'admin123', '系统管理员', 'admin', NOW(), NOW()),
  (2, 'student1', '123456', '张三', 'student', NOW(), NOW()),
  (3, 'teacher1', '123456', '李四', 'teacher', NOW(), NOW());

-- 2. 学生表 ------------------------------------------------
INSERT INTO student (id, user_id, grade, class_name) VALUES 
  (1, 2, '2022', '计科1班');

-- 3. 教师表 ------------------------------------------------
INSERT INTO teacher (id, user_id, title, department) VALUES
  (1, 3, '副教授', '计算机学院');

-- 4. 课程表 ------------------------------------------------
INSERT INTO course (id, class_code, course_name, credit, hours, description, created_at) VALUES
  (1, 'CS101', '数据结构', 3.0, 48, '数据结构课程简介', NOW());

-- 5. 教室表 ------------------------------------------------
INSERT INTO classroom (id, building, classroom_name, capacity) VALUES
  (1, 'A', '101', 60);

-- 6. 教学班表 ------------------------------------------------
INSERT INTO teaching_class (id, class_code, max_students, current_students, course_id, teacher_id) VALUES
  (1, 'TC101', 60, 0, 1, 1);

-- 7. 课程时间表 ------------------------------------------------
INSERT INTO class_schedule (id, day_of_week, start_time, end_time, classroom_id, teaching_class_id) VALUES
  (1, 1, '08:00', '10:00', 1, 1);

-- 8. 选课表 ------------------------------------------------
INSERT INTO course_selection (id, student_id, teaching_class_id, selection_time) VALUES
  (1, 1, 1, NOW());
