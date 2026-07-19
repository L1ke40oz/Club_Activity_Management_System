package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("club_info")
public class ClubInfo {

    @TableId(type = IdType.AUTO)
    private Integer clubId;

    private String name;

    private String category;

    private LocalDate establishedDate;

    private String advisor;

    private String creatorId;

    private String description;

    private Integer maxMembers;

    private Integer status; // 0-待审批, 1-正常, 2-已驳回

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
}
