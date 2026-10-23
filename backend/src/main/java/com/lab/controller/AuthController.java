package com.lab.controller;

import com.lab.common.Result;
import com.lab.entity.User;
import com.lab.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口：登录、注册、获取当前用户
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return Result.ok("登录成功", authService.login(body.get("username"), body.get("password")));
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return Result.ok("注册成功", authService.register(user));
    }

    @GetMapping("/me")
    public Result<User> me(@RequestAttribute("uid") Integer uid) {
        return Result.ok(authService.getById(uid));
    }
}
