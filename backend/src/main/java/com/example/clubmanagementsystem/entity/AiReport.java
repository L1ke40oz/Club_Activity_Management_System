package com.example.clubmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_report")
public class AiReport {

    @TableId(type = IdType.AUTO)
    private Integer reportId;

    private Integer clubId;

    private String timeRange;

    private String evaluationAndSuggestion;

    private LocalDateTime createTime;
}
