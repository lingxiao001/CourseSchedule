package com.example.courseschedule.controller;

import com.example.courseschedule.dto.UserCreateDTO;
import com.example.courseschedule.dto.UserDTO;
import com.example.courseschedule.dto.UserUpdateDTO;
import com.example.courseschedule.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private UserService userService;
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    public Page<UserDTO> getUsers(@PageableDefault(size = 10) Pageable pageable) {
        return userService.getAllUsers(pageable).map(UserDTO::fromUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody UserCreateDTO dto) {
        return UserDTO.fromUser(userService.createUser(dto));
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return UserDTO.fromUser(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO dto) {
        return UserDTO.fromUser(userService.updateUser(userId, dto));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}