package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_member")
public class ClubMember {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer clubId;

    private String studentId;

    private String position;

    private LocalDateTime joinTime;

    private Integer status; // 0-待审批, 1-已通过, 2-已退出

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String className;
}
