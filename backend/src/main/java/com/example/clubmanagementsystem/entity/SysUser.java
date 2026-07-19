package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.INPUT)
    private String studentId;

    private String name;

    private String password;

    private Integer role; // 0-普通用户, 1-系统管理员

    private String className;

    private String contactInfo;

    private Integer noShowCount; // 缺勤次数

    private LocalDateTime createTime;
}