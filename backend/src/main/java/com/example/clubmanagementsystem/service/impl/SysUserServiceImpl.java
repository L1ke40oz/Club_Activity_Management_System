package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.mapper.SysUserMapper;
import com.example.clubmanagementsystem.service.SysUserService;
import org.springframework.stereotype.Service;

@Service // 这个注解极其重要，告诉 Spring 这是一个被它管理的业务管家
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    // 基础的业务逻辑 MyBatis-Plus 已经帮你写好了
}