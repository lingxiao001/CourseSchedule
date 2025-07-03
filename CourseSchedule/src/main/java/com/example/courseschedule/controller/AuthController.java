package com.example.courseschedule.controller;

import com.example.courseschedule.dto.AuthResponseDTO;
import com.example.courseschedule.dto.LoginDTO;
import com.example.courseschedule.dto.UserCreateDTO;
import com.example.courseschedule.entity.User;
import com.example.courseschedule.entity.User.Role;
import com.example.courseschedule.service.AuthService;
import com.example.courseschedule.service.UserService;
import com.example.courseschedule.service.TeacherClassService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TeacherClassService teacherRepository;
    private final UserService userService;
    public AuthController(AuthService authService,
                          TeacherClassService teacherRepository,
                          UserService userService) {
        this.authService = authService;
        this.teacherRepository=teacherRepository;
        this.userService=userService;
    }
    
    @GetMapping("/me")
    public AuthResponseDTO getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("未登录");
        }
        return new AuthResponseDTO(user);
    }
    
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        User user = authService.authenticate(loginDTO);
        // 将用户信息保存到 session，便于后续接口获取
        session.setAttribute("currentUser", user);
        return new AuthResponseDTO(user);
    }

    /**
     * 用户注册接口（仅支持学生和教师角色）
     */
    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody UserCreateDTO userCreateDTO, HttpSession session) {
        // 仅允许学生或教师注册
        if (userCreateDTO.getRole() == null ||
                (userCreateDTO.getRole() != Role.student && userCreateDTO.getRole() != Role.teacher)) {
            throw new IllegalArgumentException("注册仅支持学生或教师角色");
        }

        User newUser = userService.createUser(userCreateDTO);
        session.setAttribute("currentUser", newUser);
        return new AuthResponseDTO(newUser);
    }
}