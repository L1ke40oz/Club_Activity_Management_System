package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_comment")
public class ClubComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer clubId;
    private String studentId;
    private String content;
    private Integer rating; // 1-5 stars
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String studentName;
}
