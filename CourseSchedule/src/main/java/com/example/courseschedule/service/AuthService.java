package com.example.courseschedule.service;

import com.example.courseschedule.dto.LoginDTO;
import com.example.courseschedule.entity.User;
import com.example.courseschedule.entity.User.Role;
import com.example.courseschedule.repository.UserRepository;

import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        
        // 验证密码
        boolean passwordMatch;
        try {
            passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        } catch (Exception e) {
            passwordMatch = false; // 处理非法格式等异常
        }
        if (!passwordMatch) {
            // 兼容历史数据：数据库中存储的是明文密码
            if (loginDTO.getPassword().equals(user.getPassword())) {
                // 升级为加密存储
                user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
                userRepository.save(user);
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
            }
        }
        
        // 立即加载关联实体（根据实际实体关系调整）
        if (user.getRole() == Role.student) {
            Hibernate.initialize(user.getStudent()); // 手动初始化
        } else if (user.getRole() == Role.teacher) {
            Hibernate.initialize(user.getTeacher());
        }
        
        return user;
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        // 假设当前用户信息通过 ThreadLocal 或 JWT 存储
        // 示例：从 ThreadLocal 获取当前用户
        ThreadLocal<User> threadLocal = new ThreadLocal<>();
        User currentUser = threadLocal.get();

        if (currentUser == null) {
            throw new RuntimeException("当前用户未登录");
        }

        return currentUser;
    }
}