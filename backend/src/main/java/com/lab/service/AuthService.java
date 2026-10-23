package com.lab.service;

import com.lab.common.BizException;
import com.lab.dao.UserDao;
import com.lab.entity.OperationLog;
import com.lab.entity.User;
import com.lab.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务：登录、注册
 */
@Service
public class AuthService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    private final LogService logService;

    public AuthService(UserDao userDao, JwtUtil jwtUtil, LogService logService) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
        this.logService = logService;
    }

    /**
     * 登录：校验用户名密码，成功后签发 Token
     */
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BizException("用户名和密码不能为空");
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null || !user.getPassword().equals(password)) {
            logService.record(user == null ? null : user.getId(), username,
                    "登录失败", "用户名或密码错误");
            throw new BizException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        logService.record(user.getId(), user.getUsername(), "登录", "登录成功");
        return data;
    }

    /**
     * 注册：默认学生角色，不允许自助注册管理员
     */
    public User register(User form) {
        if (form.getUsername() == null || form.getUsername().trim().length() < 3) {
            throw new BizException("用户名长度不能少于 3 个字符");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new BizException("密码长度不能少于 6 位");
        }
        String role = form.getRole() == null ? "student" : form.getRole();
        if (!"student".equals(role) && !"teacher".equals(role)) {
            throw new BizException("仅允许注册学生或教师账号");
        }
        String username = form.getUsername().trim();
        if (userDao.findByUsername(username) != null) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(form.getPassword());
        user.setRole(role);
        user.setRealName(form.getRealName());
        int id = userDao.insert(user);
        if (id <= 0) {
            throw new BizException("注册失败");
        }
        user.setId(id);
        user.setPassword(null);
        logService.record(id, username, "注册", "新用户注册，角色：" + role);
        return user;
    }

    public User getById(Integer id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }
}
