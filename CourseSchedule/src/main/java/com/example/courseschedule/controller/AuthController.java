package com.example.courseschedule.controller;

import com.example.courseschedule.dto.AuthResponseDTO;
import com.example.courseschedule.dto.LoginDTO;
import com.example.courseschedule.entity.Teacher;
import com.example.courseschedule.entity.User;
import com.example.courseschedule.service.AuthService;
import com.example.courseschedule.service.TeacherClassService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TeacherClassService teacherRepository;
    public AuthController(AuthService authService,TeacherClassService teacherRepository) {
        this.authService = authService;
        this.teacherRepository=teacherRepository;
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
    public AuthResponseDTO login(@RequestBody LoginDTO loginDTO) {
        User user = authService.authenticate(loginDTO);
        return new AuthResponseDTO(user);
    }
}