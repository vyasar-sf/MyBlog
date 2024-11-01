package com.practical.myblog.controller;

import com.practical.myblog.dto.AuthenticationRequestDTO;
import com.practical.myblog.dto.AuthenticationResponseDTO;
import com.practical.myblog.dto.UserRequestDTO;
import com.practical.myblog.dto.UserResponseDTO;
import com.practical.myblog.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public Page<UserResponseDTO> getPosts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return userServiceImpl.getAllUsers(pageNo, pageSize);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable Long id) {
        return userServiceImpl.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userServiceImpl.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/demo")
    public ResponseEntity<String> authenticationDemo() {
        return ResponseEntity.ok("Reached secured endpoint");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody AuthenticationRequestDTO request) {
        return ResponseEntity.ok(userServiceImpl.authenticateUser(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userServiceImpl.registerUser(request));
    }
}
