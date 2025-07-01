package com.example.courseschedule.service;

import com.example.courseschedule.dto.LoginDTO;
import com.example.courseschedule.entity.User;
import com.example.courseschedule.entity.User.Role;
import com.example.courseschedule.repository.UserRepository;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 密码验证逻辑...
        
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