package org.art.mt.controller;

import org.art.mt.entity.User;
import org.art.mt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
 
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if(userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword())) {
            return ResponseEntity.ok("User registered successfully");
        }
        return ResponseEntity.badRequest().body("User registration failed");
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
