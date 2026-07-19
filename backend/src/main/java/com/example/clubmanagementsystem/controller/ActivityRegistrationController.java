package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.ActivityInfo;
import com.example.clubmanagementsystem.entity.ActivityRegistration;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.ActivityInfoService;
import com.example.clubmanagementsystem.service.ActivityRegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/registration")
public class ActivityRegistrationController {

    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private com.example.clubmanagementsystem.service.SysUserService sysUserService;

    @Autowired
    private com.example.clubmanagementsystem.service.ClubMemberService clubMemberService;

    @PostMapping("/signup/{activityId}")
    public Result<String> signUp(@PathVariable Integer activityId, HttpSession session) {
        SysUser sessionUser = (SysUser) session.getAttribute("currentUser");
        if (sessionUser == null) {
            return Result.error(401, "未登录");
        }

        // 从数据库获取最新的用户数据（session中的可能过期）
        SysUser currentUser = sysUserService.getById(sessionUser.getStudentId());

        // 检查缺勤次数限制
        int noShowCount = currentUser.getNoShowCount() != null ? currentUser.getNoShowCount() : 0;
        if (noShowCount >= 3) {
            return Result.error(403, "您累计缺勤 " + noShowCount + " 次，已被限制报名活动。请联系管理员解除限制。");
        }

        ActivityInfo activity = activityInfoService.getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return Result.error(400, "活动未发布或不存在，无法报名");
        }

        // 校验是否为该社团的正式成员（管理员除外）
        if (currentUser.getRole() != 1) {
            QueryWrapper<com.example.clubmanagementsystem.entity.ClubMember> memberWrapper = new QueryWrapper<>();
            memberWrapper.eq("club_id", activity.getClubId())
                         .eq("student_id", currentUser.getStudentId())
                         .eq("status", 1);
            if (clubMemberService.count(memberWrapper) == 0) {
                return Result.error(403, "只有该社团成员才能报名活动，请先加入社团");
            }
        }

        // 检查人数上限
        if (activity.getMaxParticipants() != null) {
            QueryWrapper<ActivityRegistration> countWrapper = new QueryWrapper<>();
            countWrapper.eq("activity_id", activityId);
            if (registrationService.count(countWrapper) >= activity.getMaxParticipants()) {
                return Result.error(400, "活动报名人数已满");
            }
        }

        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId)
               .eq("student_id", currentUser.getStudentId());
        if (registrationService.count(wrapper) > 0) {
            return Result.error(400, "已报名该活动");
        }

        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(activityId);
        reg.setStudentId(currentUser.getStudentId());
        reg.setRegTime(LocalDateTime.now());
        reg.setSignInStatus(0);
        registrationService.save(reg);
        return Result.success("报名成功");
    }

    @PutMapping("/signin/{id}")
    public Result<String> signIn(@PathVariable Integer id) {
        ActivityRegistration reg = registrationService.getById(id);
        if (reg == null) {
            return Result.error(404, "报名记录不存在");
        }
        reg.setSignInStatus(1);
        reg.setSignInTime(LocalDateTime.now());
        registrationService.updateById(reg);
        return Result.success("签到成功");
    }

    @PutMapping("/signin/batch/{activityId}")
    public Result<String> batchSignIn(@PathVariable Integer activityId, @RequestBody List<String> studentIds) {
        for (String studentId : studentIds) {
            QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
            wrapper.eq("activity_id", activityId)
                   .eq("student_id", studentId);
            ActivityRegistration reg = registrationService.getOne(wrapper);
            if (reg != null && reg.getSignInStatus() == 0) {
                reg.setSignInStatus(1);
                reg.setSignInTime(LocalDateTime.now());
                registrationService.updateById(reg);
            }
        }
        return Result.success("批量签到完成");
    }

    @GetMapping("/list/{activityId}")
    public Result<List<ActivityRegistration>> listByActivity(@PathVariable Integer activityId) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        List<ActivityRegistration> list = registrationService.list(wrapper);
        for (ActivityRegistration reg : list) {
            ActivityInfo activity = activityInfoService.getById(reg.getActivityId());
            if (activity != null) {
                reg.setActivityTitle(activity.getTitle());
                reg.setActivityStatus(activity.getStatus());
            }
        }
        return Result.success(list);
    }

    // 管理员：获取所有报名记录
    @GetMapping("/all")
    public Result<List<ActivityRegistration>> allRegistrations() {
        List<ActivityRegistration> list = registrationService.list();
        for (ActivityRegistration reg : list) {
            ActivityInfo activity = activityInfoService.getById(reg.getActivityId());
            if (activity != null) {
                reg.setActivityTitle(activity.getTitle());
                reg.setActivityStatus(activity.getStatus());
            }
        }
        return Result.success(list);
    }

    @GetMapping("/my")
    public Result<List<ActivityRegistration>> myRegistrations(HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", currentUser.getStudentId());
        List<ActivityRegistration> list = registrationService.list(wrapper);

        // 补充活动标题和活动状态
        for (ActivityRegistration reg : list) {
            ActivityInfo activity = activityInfoService.getById(reg.getActivityId());
            if (activity != null) {
                reg.setActivityTitle(activity.getTitle());
                reg.setActivityStatus(activity.getStatus());
            }
        }

        return Result.success(list);
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancelRegistration(@PathVariable Integer id, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }

        ActivityRegistration reg = registrationService.getById(id);
        if (reg == null) {
            return Result.error(404, "报名记录不存在");
        }
        if (!reg.getStudentId().equals(currentUser.getStudentId())) {
            return Result.error(403, "只能取消自己的报名");
        }
        if (reg.getSignInStatus() == 1) {
            return Result.error(400, "已签到的报名无法取消");
        }

        registrationService.removeById(id);
        return Result.success("取消报名成功");
    }
}
