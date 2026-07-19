package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.SysUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        SysUser existing = sysUserService.getById(user.getStudentId());
        if (existing != null) {
            return Result.error(400, "该学号已注册");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        if (user.getRole() == null) {
            user.setRole(0);
        }
        sysUserService.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm, HttpSession session) {
        String studentId = loginForm.get("studentId");
        String password = loginForm.get("password");

        SysUser user = sysUserService.getById(studentId);
        if (user == null) {
            return Result.error(400, "用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(400, "密码错误");
        }

        session.setAttribute("currentUser", user);

        Map<String, Object> data = new HashMap<>();
        data.put("studentId", user.getStudentId());
        data.put("name", user.getName());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功");
    }

    @GetMapping("/info")
    public Result<SysUser> getCurrentUser(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        SysUser fresh = sysUserService.getById(user.getStudentId());
        fresh.setPassword(null);
        return Result.success(fresh);
    }

    @GetMapping("/list")
    public Result<List<SysUser>> getUserList() {
        List<SysUser> list = sysUserService.list();
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        sysUserService.updateById(user);
        return Result.success("更新成功");
    }
}
