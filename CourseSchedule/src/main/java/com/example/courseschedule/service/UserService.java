package com.example.courseschedule.service;

import com.example.courseschedule.dto.UserCreateDTO;
import com.example.courseschedule.dto.UserUpdateDTO;
import com.example.courseschedule.entity.*;
import com.example.courseschedule.entity.User.Role;
import com.example.courseschedule.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       TeacherRepository teacherRepository,
                       PasswordEncoder passwordEncoder
                        ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> getAllUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(search.trim(), pageable);
    }

    @Transactional
    public User createUser(UserCreateDTO userDTO) {
        // 验证角色特定字段
        validateRoleSpecificFields(userDTO);

        // 创建基础用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRealName(userDTO.getRealName());
        user.setRole(userDTO.getRole());

        // 根据角色处理特定逻辑
        if (userDTO.getRole() == Role.teacher) {
             createTeacher(user, userDTO);
        } else if (userDTO.getRole() == Role.student) {
           createStudent(user, userDTO);
        }

        return userRepository.save(user);
    }

    private void validateRoleSpecificFields(UserCreateDTO userDTO) {
        if (userDTO.getRole() == Role.student) {
            if (userDTO.getStudentId() == null || userDTO.getGrade() == null || userDTO.getClassName() == null) {
                throw new IllegalArgumentException("创建学生用户必须提供学号、年级和班级信息");
            }
        } else if (userDTO.getRole() == Role.teacher) {
            if (userDTO.getTeacherId() == null || userDTO.getTitle() == null || userDTO.getDepartment() == null) {
                throw new IllegalArgumentException("创建教师用户必须提供教师ID、职称和部门信息");
            }
        }
    }

    private void createStudent(User user, UserCreateDTO userDTO) {
        Student student = new Student();
        student.setId(userDTO.getStudentId());
        student.setGrade(userDTO.getGrade());
        student.setClassName(userDTO.getClassName());
        student.setUser(user);
        user.setStudent(student);
    }

    private void createTeacher(User user, UserCreateDTO userDTO) {
        Teacher teacher = new Teacher();
        teacher.setId(userDTO.getTeacherId());
        teacher.setTitle(userDTO.getTitle());
        teacher.setDepartment(userDTO.getDepartment());
        teacher.setUser(user);
        user.setTeacher(teacher);    
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateDTO userDTO) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (userDTO.getRealName() != null) {
            existingUser.setRealName(userDTO.getRealName());
        }
        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }
        if (userDTO.getNewPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
        }

        if (existingUser.getRole() == Role.student) {
            Student student = existingUser.getStudent();
            if (student != null) {
                if (userDTO.getGrade() != null) {
                    student.setGrade(userDTO.getGrade());
                }
                if (userDTO.getClassName() != null) {
                    student.setClassName(userDTO.getClassName());
                }
            }
        } else if (existingUser.getRole() == Role.teacher) {
            Teacher teacher = existingUser.getTeacher();
            if (teacher != null) {
                if (userDTO.getTitle() != null) {
                    teacher.setTitle(userDTO.getTitle());
                }
                if (userDTO.getDepartment() != null) {
                    teacher.setDepartment(userDTO.getDepartment());
                }
            }
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getRole() == Role.student) {
            Student student = user.getStudent();
            if (student != null) {
                studentRepository.delete(student);
            }
        } else if (user.getRole() == Role.teacher) {
            Teacher teacher = user.getTeacher();
            if (teacher != null) {
                teacherRepository.delete(teacher);
            }
        }
        userRepository.delete(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public void resetPassword(String username, String oldPassword, String newPassword) {
        log.debug("尝试为用户 {} 重置密码", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("用户 {} 不存在", username);
                return new IllegalArgumentException("用户不存在");
            });
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // 兼容明文密码
            if (!oldPassword.equals(user.getPassword())) {
                log.warn("用户 {} 原密码校验失败，输入: {}, 数据库: {}", username, oldPassword, user.getPassword());
                throw new IllegalArgumentException("原密码错误");
            }
            // 升级为加密存储
            log.info("用户 {} 原密码为明文，已兼容校验并升级为加密存储", username);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return;
        }
        log.info("用户 {} 原密码加密校验通过，重置密码成功", username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
