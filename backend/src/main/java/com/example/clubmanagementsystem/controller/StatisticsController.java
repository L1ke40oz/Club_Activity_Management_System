package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.ActivityInfo;
import com.example.clubmanagementsystem.entity.ActivityRegistration;
import com.example.clubmanagementsystem.entity.ClubInfo;
import com.example.clubmanagementsystem.entity.ClubMember;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private ClubInfoService clubInfoService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();

        long clubCount = clubInfoService.count(new QueryWrapper<ClubInfo>().eq("status", 1));
        long memberCount = clubMemberService.count(new QueryWrapper<ClubMember>().eq("status", 1));
        long activityCount = activityInfoService.count();
        long publishedActivityCount = activityInfoService.count(new QueryWrapper<ActivityInfo>().eq("status", 1));
        long finishedActivityCount = activityInfoService.count(new QueryWrapper<ActivityInfo>().eq("status", 2));
        long registrationCount = registrationService.count();
        long signedInCount = registrationService.count(new QueryWrapper<ActivityRegistration>().eq("sign_in_status", 1));
        long userCount = sysUserService.count();

        data.put("clubCount", clubCount);
        data.put("memberCount", memberCount);
        data.put("activityCount", activityCount);
        data.put("publishedActivityCount", publishedActivityCount);
        data.put("finishedActivityCount", finishedActivityCount);
        data.put("registrationCount", registrationCount);
        data.put("signedInCount", signedInCount);
        data.put("attendanceRate", registrationCount > 0 ? Math.round(signedInCount * 100.0 / registrationCount) : 0);
        data.put("userCount", userCount);

        return Result.success(data);
    }

    @GetMapping("/clubs")
    public Result<List<Map<String, Object>>> clubStats(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        QueryWrapper<ClubInfo> clubWrapper = new QueryWrapper<>();
        clubWrapper.eq("status", 1);
        if (name != null && !name.isEmpty()) {
            clubWrapper.like("name", name);
        }
        if (category != null && !category.isEmpty()) {
            clubWrapper.eq("category", category);
        }
        List<ClubInfo> clubs = clubInfoService.list(clubWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubInfo club : clubs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("clubId", club.getClubId());
            item.put("name", club.getName());
            item.put("category", club.getCategory());
            item.put("advisor", club.getAdvisor());
            item.put("establishedDate", club.getEstablishedDate());

            long memberCnt = clubMemberService.count(
                    new QueryWrapper<ClubMember>().eq("club_id", club.getClubId()).eq("status", 1));
            item.put("memberCount", memberCnt);

            long actCnt = activityInfoService.count(
                    new QueryWrapper<ActivityInfo>().eq("club_id", club.getClubId()));
            item.put("activityCount", actCnt);

            long finishedCnt = activityInfoService.count(
                    new QueryWrapper<ActivityInfo>().eq("club_id", club.getClubId()).eq("status", 2));
            item.put("finishedActivityCount", finishedCnt);

            // 该社团所有活动的报名和签到
            List<ActivityInfo> clubActivities = activityInfoService.list(
                    new QueryWrapper<ActivityInfo>().eq("club_id", club.getClubId()));
            long totalReg = 0, totalSign = 0;
            for (ActivityInfo act : clubActivities) {
                totalReg += registrationService.count(
                        new QueryWrapper<ActivityRegistration>().eq("activity_id", act.getActivityId()));
                totalSign += registrationService.count(
                        new QueryWrapper<ActivityRegistration>().eq("activity_id", act.getActivityId()).eq("sign_in_status", 1));
            }
            item.put("totalRegistrations", totalReg);
            item.put("totalSignIns", totalSign);
            item.put("attendanceRate", totalReg > 0 ? Math.round(totalSign * 100.0 / totalReg) : 0);

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/activities")
    public Result<List<Map<String, Object>>> activityStats(
            @RequestParam(required = false) Integer clubId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        QueryWrapper<ActivityInfo> wrapper = new QueryWrapper<>();
        if (clubId != null) {
            wrapper.eq("club_id", clubId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("event_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("event_time", endDate);
        }
        wrapper.orderByDesc("event_time");
        List<ActivityInfo> activities = activityInfoService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityInfo act : activities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("activityId", act.getActivityId());
            item.put("title", act.getTitle());
            item.put("clubId", act.getClubId());
            item.put("eventTime", act.getEventTime());
            item.put("location", act.getLocation());
            item.put("status", act.getStatus());

            // 获取社团名称
            ClubInfo club = clubInfoService.getById(act.getClubId());
            item.put("clubName", club != null ? club.getName() : "未知");

            long regCnt = registrationService.count(
                    new QueryWrapper<ActivityRegistration>().eq("activity_id", act.getActivityId()));
            long signCnt = registrationService.count(
                    new QueryWrapper<ActivityRegistration>().eq("activity_id", act.getActivityId()).eq("sign_in_status", 1));
            item.put("registrationCount", regCnt);
            item.put("signInCount", signCnt);
            item.put("attendanceRate", regCnt > 0 ? Math.round(signCnt * 100.0 / regCnt) : 0);

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/member-activity")
    public Result<List<Map<String, Object>>> memberActivityStats(
            @RequestParam(required = false) Integer clubId) {

        QueryWrapper<ClubMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("status", 1);
        if (clubId != null) {
            memberWrapper.eq("club_id", clubId);
        }
        List<ClubMember> members = clubMemberService.list(memberWrapper);

        Map<String, Map<String, Object>> userStatsMap = new LinkedHashMap<>();
        for (ClubMember member : members) {
            String sid = member.getStudentId();
            if (userStatsMap.containsKey(sid)) {
                Map<String, Object> existing = userStatsMap.get(sid);
                existing.put("clubCount", (int) existing.get("clubCount") + 1);
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", sid);

            SysUser user = sysUserService.getById(sid);
            item.put("name", user != null ? user.getName() : "未知");
            item.put("className", user != null ? user.getClassName() : "");

            item.put("clubCount", 1);

            long regCnt = registrationService.count(
                    new QueryWrapper<ActivityRegistration>().eq("student_id", sid));
            long signCnt = registrationService.count(
                    new QueryWrapper<ActivityRegistration>().eq("student_id", sid).eq("sign_in_status", 1));
            item.put("registrationCount", regCnt);
            item.put("signInCount", signCnt);
            item.put("attendanceRate", regCnt > 0 ? Math.round(signCnt * 100.0 / regCnt) : 0);
            item.put("noShowCount", user != null ? user.getNoShowCount() : 0);

            userStatsMap.put(sid, item);
        }

        return Result.success(new ArrayList<>(userStatsMap.values()));
    }
}
