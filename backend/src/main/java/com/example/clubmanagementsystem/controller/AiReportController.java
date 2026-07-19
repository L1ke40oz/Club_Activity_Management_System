package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.AiReport;
import com.example.clubmanagementsystem.service.AiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-report")
public class AiReportController {

    @Autowired
    private AiReportService aiReportService;

    @PostMapping("/generate")
    public Result<AiReport> generate(@RequestBody Map<String, Object> params) {
        String timeRange = (String) params.get("timeRange");
        if (timeRange == null || timeRange.isEmpty()) {
            return Result.error(400, "请指定统计周期");
        }

        try {
            Object clubIdObj = params.get("clubId");
            Integer clubId = null;
            if (clubIdObj != null) {
                clubId = clubIdObj instanceof Integer ? (Integer) clubIdObj : Integer.parseInt(clubIdObj.toString());
            }

            AiReport report;
            if (clubId != null) {
                report = aiReportService.generateReport(clubId, timeRange);
            } else {
                report = aiReportService.generateGlobalReport(timeRange);
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(500, "报告生成失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<AiReport>> list(@RequestParam(required = false) Integer clubId) {
        QueryWrapper<AiReport> wrapper = new QueryWrapper<>();
        if (clubId != null) {
            wrapper.eq("club_id", clubId);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(aiReportService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<AiReport> getById(@PathVariable Integer id) {
        return Result.success(aiReportService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        AiReport report = aiReportService.getById(id);
        if (report == null) {
            return Result.error(404, "报告不存在");
        }
        aiReportService.removeById(id);
        return Result.success("报告已删除");
    }

    @GetMapping("/generate/stream")
    public SseEmitter generateStream(@RequestParam(required = false) Integer clubId,
                                     @RequestParam String timeRange) {
        SseEmitter emitter = new SseEmitter(120_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        new Thread(() -> {
            if (clubId != null) {
                aiReportService.generateReportStream(clubId, timeRange, emitter);
            } else {
                aiReportService.generateGlobalReportStream(timeRange, emitter);
            }
        }).start();

        return emitter;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@RequestBody Map<String, Object> params) {
        SseEmitter emitter = new SseEmitter(120_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        List<Map<String, String>> messages = (List<Map<String, String>>) params.get("messages");
        Integer clubId = params.get("clubId") != null ?
                Integer.parseInt(params.get("clubId").toString()) : null;

        new Thread(() -> aiReportService.chatStream(messages, clubId, emitter)).start();

        return emitter;
    }
}
