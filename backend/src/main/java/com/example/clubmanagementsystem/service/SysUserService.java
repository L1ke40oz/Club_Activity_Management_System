package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    // 这里以后可以写复杂的自定义业务，比如：带验证码的注册逻辑
}