package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.AiReport;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AiReportService extends IService<AiReport> {

    AiReport generateReport(Integer clubId, String timeRange);

    AiReport generateGlobalReport(String timeRange);

    void generateReportStream(Integer clubId, String timeRange, SseEmitter emitter);

    void generateGlobalReportStream(String timeRange, SseEmitter emitter);

    void chatStream(List<Map<String, String>> messages, Integer clubId, SseEmitter emitter);
}
