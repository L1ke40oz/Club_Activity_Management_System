package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.*;
import com.example.clubmanagementsystem.mapper.AiReportMapper;
import com.example.clubmanagementsystem.mapper.ActivityInfoMapper;
import com.example.clubmanagementsystem.mapper.ActivityRegistrationMapper;
import com.example.clubmanagementsystem.mapper.ClubMemberMapper;
import com.example.clubmanagementsystem.mapper.ClubInfoMapper;
import com.example.clubmanagementsystem.service.AiClientService;
import com.example.clubmanagementsystem.service.AiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiReportServiceImpl extends ServiceImpl<AiReportMapper, AiReport> implements AiReportService {

    @Autowired
    private AiClientService aiClientService;
    @Autowired
    private ClubInfoMapper clubInfoMapper;
    @Autowired
    private ClubMemberMapper clubMemberMapper;
    @Autowired
    private ActivityInfoMapper activityInfoMapper;
    @Autowired
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Override
    public AiReport generateReport(Integer clubId, String timeRange) {
        ClubInfo club = clubInfoMapper.selectById(clubId);
        String clubName = club != null ? club.getName() : "未知社团";

        long memberCount = clubMemberMapper.selectCount(
                new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, clubId).eq(ClubMember::getStatus, 1));

        List<ActivityInfo> activities = activityInfoMapper.selectList(
                new LambdaQueryWrapper<ActivityInfo>().eq(ActivityInfo::getClubId, clubId).ne(ActivityInfo::getIsDeleted, 1));

        int activityCount = activities.size();
        long totalSignIn = 0;
        long totalReg = 0;
        for (ActivityInfo act : activities) {
            totalReg += activityRegistrationMapper.selectCount(
                    new LambdaQueryWrapper<ActivityRegistration>().eq(ActivityRegistration::getActivityId, act.getActivityId()));
            totalSignIn += activityRegistrationMapper.selectCount(
                    new LambdaQueryWrapper<ActivityRegistration>().eq(ActivityRegistration::getActivityId, act.getActivityId())
                            .eq(ActivityRegistration::getSignInStatus, 1));
        }

        String prompt = buildClubPrompt(clubName, timeRange, memberCount, activityCount, totalReg, totalSignIn);
        String aiResult = aiClientService.chat(prompt);

        AiReport report = new AiReport();
        report.setClubId(clubId);
        report.setTimeRange(timeRange);
        report.setEvaluationAndSuggestion(aiResult);
        report.setCreateTime(LocalDateTime.now());
        save(report);
        return report;
    }

    @Override
    public AiReport generateGlobalReport(String timeRange) {
        List<ClubInfo> clubs = clubInfoMapper.selectList(
                new LambdaQueryWrapper<ClubInfo>().eq(ClubInfo::getIsDeleted, 0));

        long totalActivities = activityInfoMapper.selectCount(
                new LambdaQueryWrapper<ActivityInfo>().ne(ActivityInfo::getIsDeleted, 1));

        StringBuilder dataSummary = new StringBuilder();
        dataSummary.append("全校社团总数：").append(clubs.size()).append("个\n");
        dataSummary.append("统计周期：").append(timeRange).append("\n");
        dataSummary.append("活动总数：").append(totalActivities).append("场\n\n");
        dataSummary.append("各社团概况：\n");

        for (ClubInfo club : clubs) {
            long members = clubMemberMapper.selectCount(
                    new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, club.getClubId()).eq(ClubMember::getStatus, 1));
            long acts = activityInfoMapper.selectCount(
                    new LambdaQueryWrapper<ActivityInfo>().eq(ActivityInfo::getClubId, club.getClubId()).ne(ActivityInfo::getIsDeleted, 1));
            dataSummary.append("- ").append(club.getName()).append("：成员").append(members).append("人，活动").append(acts).append("场\n");
        }

        String prompt = "你是一位高校社团管理顾问和数据分析师。请根据以下全校社团运营数据，撰写一份全面的运营评估报告。\n\n"
                + "## 全校概况\n"
                + dataSummary
                + "\n## 报告要求\n"
                + "请使用Markdown格式，从以下维度分析：\n"
                + "1. **整体运营评价**：全校社团生态的总体健康度\n"
                + "2. **活跃度排名**：哪些社团表现突出，哪些需要关注\n"
                + "3. **资源配置建议**：校团委应如何分配活动场地、经费等资源\n"
                + "4. **发展趋势预判**：基于当前数据的发展建议\n\n"
                + "请用中文回复，控制在500字以内，语言专业但通俗易懂。";

        String aiResult = aiClientService.chat(prompt);

        AiReport report = new AiReport();
        report.setClubId(null);
        report.setTimeRange(timeRange);
        report.setEvaluationAndSuggestion(aiResult);
        report.setCreateTime(LocalDateTime.now());
        save(report);
        return report;
    }

    private String buildClubPrompt(String clubName, String timeRange, long memberCount, int activityCount, long totalReg, long totalSignIn) {
        double attendanceRate = totalReg > 0 ? (double) totalSignIn / totalReg * 100 : 0;
        double avgRegPerActivity = activityCount > 0 ? (double) totalReg / activityCount : 0;

        return "你是一位专业的高校社团运营分析师。请根据以下数据，为社团撰写一份运营评估报告。\n\n"
                + "## 分析对象\n"
                + "社团名称：" + clubName + "\n"
                + "统计周期：" + timeRange + "\n\n"
                + "## 运营数据\n"
                + "| 指标 | 数值 |\n"
                + "|------|------|\n"
                + "| 当前成员数 | " + memberCount + " 人 |\n"
                + "| 举办活动数 | " + activityCount + " 场 |\n"
                + "| 活动总报名人次 | " + totalReg + " |\n"
                + "| 实际签到人次 | " + totalSignIn + " |\n"
                + "| 平均签到率 | " + String.format("%.1f", attendanceRate) + "% |\n"
                + "| 场均报名人数 | " + String.format("%.1f", avgRegPerActivity) + " |\n\n"
                + "## 报告要求\n"
                + "请从以下维度进行分析，使用Markdown格式输出：\n"
                + "1. **运营概况**：总体评价社团的活跃度和运营质量\n"
                + "2. **数据亮点**：指出表现优秀的方面\n"
                + "3. **问题诊断**：分析可能存在的问题（如签到率低、活动频次不足等）\n"
                + "4. **优化建议**：给出具体可操作的改进方案\n\n"
                + "请用中文回复，控制在400字以内，语言专业但通俗易懂。";
    }

    @Override
    public void generateReportStream(Integer clubId, String timeRange, SseEmitter emitter) {
        try {
            ClubInfo club = clubInfoMapper.selectById(clubId);
            String clubName = club != null ? club.getName() : "未知社团";

            long memberCount = clubMemberMapper.selectCount(
                    new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, clubId).eq(ClubMember::getStatus, 1));

            List<ActivityInfo> activities = activityInfoMapper.selectList(
                    new LambdaQueryWrapper<ActivityInfo>().eq(ActivityInfo::getClubId, clubId).ne(ActivityInfo::getIsDeleted, 1));

            int activityCount = activities.size();
            long totalSignIn = 0, totalReg = 0;
            for (ActivityInfo act : activities) {
                totalReg += activityRegistrationMapper.selectCount(
                        new LambdaQueryWrapper<ActivityRegistration>().eq(ActivityRegistration::getActivityId, act.getActivityId()));
                totalSignIn += activityRegistrationMapper.selectCount(
                        new LambdaQueryWrapper<ActivityRegistration>().eq(ActivityRegistration::getActivityId, act.getActivityId())
                                .eq(ActivityRegistration::getSignInStatus, 1));
            }

            String prompt = buildClubPrompt(clubName, timeRange, memberCount, activityCount, totalReg, totalSignIn);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一位高校社团运营分析专家，请根据提供的数据给出专业的运营评估与建议。"));
            messages.add(Map.of("role", "user", "content", prompt));

            String fullText = aiClientService.chatStream(messages, token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            AiReport report = new AiReport();
            report.setClubId(clubId);
            report.setTimeRange(timeRange);
            report.setEvaluationAndSuggestion(fullText);
            report.setCreateTime(LocalDateTime.now());
            save(report);

            emitter.send(SseEmitter.event().name("done").data("{\"reportId\":" + report.getReportId() + "}"));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "未知错误"));
            } catch (IOException ignored) {}
            emitter.completeWithError(e);
        }
    }

    @Override
    public void generateGlobalReportStream(String timeRange, SseEmitter emitter) {
        try {
            List<ClubInfo> clubs = clubInfoMapper.selectList(
                    new LambdaQueryWrapper<ClubInfo>().eq(ClubInfo::getIsDeleted, 0));

            long totalActivities = activityInfoMapper.selectCount(
                    new LambdaQueryWrapper<ActivityInfo>().ne(ActivityInfo::getIsDeleted, 1));

            StringBuilder dataSummary = new StringBuilder();
            dataSummary.append("全校社团总数：").append(clubs.size()).append("个\n");
            dataSummary.append("统计周期：").append(timeRange).append("\n");
            dataSummary.append("活动总数：").append(totalActivities).append("场\n\n");
            dataSummary.append("各社团概况：\n");

            for (ClubInfo club : clubs) {
                long members = clubMemberMapper.selectCount(
                        new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, club.getClubId()).eq(ClubMember::getStatus, 1));
                long acts = activityInfoMapper.selectCount(
                        new LambdaQueryWrapper<ActivityInfo>().eq(ActivityInfo::getClubId, club.getClubId()).ne(ActivityInfo::getIsDeleted, 1));
                dataSummary.append("- ").append(club.getName()).append("：成员").append(members).append("人，活动").append(acts).append("场\n");
            }

            String prompt = "你是一位高校社团管理顾问和数据分析师。请根据以下全校社团运营数据，撰写一份全面的运营评估报告。\n\n"
                    + "## 全校概况\n" + dataSummary
                    + "\n## 报告要求\n"
                    + "请使用Markdown格式，从以下维度分析：\n"
                    + "1. **整体运营评价**：全校社团生态的总体健康度\n"
                    + "2. **活跃度排名**：哪些社团表现突出，哪些需要关注\n"
                    + "3. **资源配置建议**：校团委应如何分配活动场地、经费等资源\n"
                    + "4. **发展趋势预判**：基于当前数据的发展建议\n\n"
                    + "请用中文回复，控制在500字以内，语言专业但通俗易懂。";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一位高校社团运营分析专家，请根据提供的数据给出专业的运营评估与建议。"));
            messages.add(Map.of("role", "user", "content", prompt));

            String fullText = aiClientService.chatStream(messages, token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            AiReport report = new AiReport();
            report.setClubId(null);
            report.setTimeRange(timeRange);
            report.setEvaluationAndSuggestion(fullText);
            report.setCreateTime(LocalDateTime.now());
            save(report);

            emitter.send(SseEmitter.event().name("done").data("{\"reportId\":" + report.getReportId() + "}"));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "未知错误"));
            } catch (IOException ignored) {}
            emitter.completeWithError(e);
        }
    }

    @Override
    public void chatStream(List<Map<String, String>> messages, Integer clubId, SseEmitter emitter) {
        try {
            List<Map<String, String>> fullMessages = new ArrayList<>();

            String systemPrompt = "你是一位高校社团运营分析助手，可以回答关于社团管理、活动策划、成员运营等方面的问题。请用中文回复。";
            if (clubId != null) {
                ClubInfo club = clubInfoMapper.selectById(clubId);
                if (club != null) {
                    long memberCount = clubMemberMapper.selectCount(
                            new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, clubId).eq(ClubMember::getStatus, 1));
                    long actCount = activityInfoMapper.selectCount(
                            new LambdaQueryWrapper<ActivityInfo>().eq(ActivityInfo::getClubId, clubId).ne(ActivityInfo::getIsDeleted, 1));
                    systemPrompt += "\n\n当前上下文社团：" + club.getName() + "，成员数：" + memberCount + "，活动数：" + actCount;
                }
            }

            fullMessages.add(Map.of("role", "system", "content", systemPrompt));
            fullMessages.addAll(messages);

            aiClientService.chatStream(fullMessages, token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            emitter.send(SseEmitter.event().name("done").data("{}"));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "未知错误"));
            } catch (IOException ignored) {}
            emitter.completeWithError(e);
        }
    }
}
