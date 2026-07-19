package com.example.clubmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.clubmanagementsystem.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 基础的 CRUD 已经由 BaseMapper 提供，这里留空即可
}