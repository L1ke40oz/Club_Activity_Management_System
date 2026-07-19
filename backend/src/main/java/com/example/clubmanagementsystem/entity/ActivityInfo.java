package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_info")
public class ActivityInfo {

    @TableId(type = IdType.AUTO)
    private Integer activityId;

    private Integer clubId;

    private String title;

    private String details;

    private LocalDateTime eventTime;

    private String location;

    private Integer maxParticipants;

    private String applicantId;

    private Integer status; // 0-待审批, 1-已发布, 2-已结束, 3-已驳回

    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private Integer regCount; // 已报名人数

    @TableField(exist = false)
    private String clubName; // 所属社团名称
}
