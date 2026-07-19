package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_registration")
public class ActivityRegistration {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer activityId;

    private String studentId;

    private LocalDateTime regTime;

    private Integer signInStatus; // 0-未签到, 1-已签到

    private LocalDateTime signInTime;

    @TableField(exist = false)
    private String activityTitle;

    @TableField(exist = false)
    private Integer activityStatus; // 活动状态：0待审批,1已发布,2已结束
}
