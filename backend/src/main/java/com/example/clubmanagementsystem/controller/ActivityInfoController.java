package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.ActivityInfo;
import com.example.clubmanagementsystem.entity.ClubMember;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.ActivityInfoService;
import com.example.clubmanagementsystem.service.ClubMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityInfoController {

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private com.example.clubmanagementsystem.service.ActivityRegistrationService registrationService;

    @Autowired
    private com.example.clubmanagementsystem.service.SysUserService sysUserService;

    @Autowired
    private com.example.clubmanagementsystem.service.SysMessageService messageService;

    @Autowired
    private com.example.clubmanagementsystem.service.ClubInfoService clubInfoService;

    @GetMapping("/list")
    public Result<List<ActivityInfo>> list(@RequestParam(required = false) Integer clubId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String title) {
        QueryWrapper<ActivityInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        if (clubId != null) {
            wrapper.eq("club_id", clubId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }
        wrapper.orderByDesc("event_time");
        List<ActivityInfo> list = activityInfoService.list(wrapper);

        // 补充已报名人数
        for (ActivityInfo act : list) {
            QueryWrapper<com.example.clubmanagementsystem.entity.ActivityRegistration> regWrapper = new QueryWrapper<>();
            regWrapper.eq("activity_id", act.getActivityId());
            act.setRegCount((int) registrationService.count(regWrapper));
        }

        // 批量补充所属社团名称（一次查询，避免 N+1）
        java.util.Set<Integer> clubIds = new java.util.HashSet<>();
        for (ActivityInfo act : list) {
            if (act.getClubId() != null) {
                clubIds.add(act.getClubId());
            }
        }
        if (!clubIds.isEmpty()) {
            java.util.Map<Integer, String> clubNameMap = new java.util.HashMap<>();
            for (com.example.clubmanagementsystem.entity.ClubInfo club : clubInfoService.listByIds(clubIds)) {
                clubNameMap.put(club.getClubId(), club.getName());
            }
            for (ActivityInfo act : list) {
                act.setClubName(clubNameMap.get(act.getClubId()));
            }
        }

        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ActivityInfo> getById(@PathVariable Integer id) {
        ActivityInfo activity = activityInfoService.getById(id);
        if (activity == null || activity.getIsDeleted() == 1) {
            return Result.error(404, "活动不存在");
        }
        // 补充所属社团名称
        if (activity.getClubId() != null) {
            com.example.clubmanagementsystem.entity.ClubInfo club = clubInfoService.getById(activity.getClubId());
            if (club != null) {
                activity.setClubName(club.getName());
            }
        }
        return Result.success(activity);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ActivityInfo activity, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        activity.setApplicantId(currentUser.getStudentId());
        activity.setIsDeleted(0);

        // 管理员创建活动直接通过
        if (currentUser.getRole() == 1) {
            activity.setStatus(1);
            activityInfoService.save(activity);
            return Result.success("活动创建成功");
        }

        // 普通用户：校验是否为该社团的社长/副社长
        QueryWrapper<ClubMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("club_id", activity.getClubId())
                     .eq("student_id", currentUser.getStudentId())
                     .eq("status", 1)
                     .in("position", "社长", "副社长", "队长");
        if (clubMemberService.count(memberWrapper) == 0) {
            return Result.error(403, "只有社长或副社长才能为该社团申请活动");
        }

        activity.setStatus(0); // 待审批
        activityInfoService.save(activity);
        return Result.success("活动申请已提交，等待管理员审批");
    }

    @PutMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Integer id) {
        ActivityInfo activity = activityInfoService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        activity.setStatus(1);
        activityInfoService.updateById(activity);
        return Result.success("活动已审批通过");
    }

    @PutMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Integer id) {
        ActivityInfo activity = activityInfoService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        activity.setStatus(3);
        activityInfoService.updateById(activity);
        return Result.success("活动已驳回");
    }

    @PutMapping("/finish/{id}")
    public Result<String> finish(@PathVariable Integer id) {
        ActivityInfo activity = activityInfoService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        activity.setStatus(2);
        activityInfoService.updateById(activity);

        // 统计未签到的报名者，累加缺勤次数并发送通知
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.clubmanagementsystem.entity.ActivityRegistration> regWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        regWrapper.eq("activity_id", id).eq("sign_in_status", 0);
        java.util.List<com.example.clubmanagementsystem.entity.ActivityRegistration> noShows = registrationService.list(regWrapper);

        for (com.example.clubmanagementsystem.entity.ActivityRegistration reg : noShows) {
            com.example.clubmanagementsystem.entity.SysUser user = sysUserService.getById(reg.getStudentId());
            if (user != null) {
                int count = (user.getNoShowCount() != null ? user.getNoShowCount() : 0) + 1;
                user.setNoShowCount(count);
                sysUserService.updateById(user);

                // 发送站内信警告
                com.example.clubmanagementsystem.entity.SysMessage msg = new com.example.clubmanagementsystem.entity.SysMessage();
                msg.setReceiverId(user.getStudentId());
                msg.setIsRead(0);
                msg.setCreateTime(java.time.LocalDateTime.now());

                if (count >= 3) {
                    msg.setTitle("⚠️ 报名资格已被限制");
                    msg.setContent("您报名了活动「" + activity.getTitle() + "」但未签到参加。"
                            + "您的累计缺勤次数已达 " + count + " 次，根据系统规则，您的活动报名资格已被暂停。如需恢复，请联系管理员。");
                } else if (count == 2) {
                    msg.setTitle("⚠️ 缺勤警告");
                    msg.setContent("您报名了活动「" + activity.getTitle() + "」但未签到参加。"
                            + "您已累计缺勤 " + count + " 次，再缺勤 1 次将被限制报名活动，请注意按时参加已报名的活动！");
                } else {
                    msg.setTitle("活动缺勤提醒");
                    msg.setContent("您报名了活动「" + activity.getTitle() + "」但未签到参加，已记录 1 次缺勤。"
                            + "累计缺勤 3 次将被限制报名，请按时参加已报名的活动。");
                }
                messageService.save(msg);
            }
        }

        return Result.success("活动已结束，已记录未签到人员缺勤（" + noShows.size() + "人）");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ActivityInfo activity) {
        activityInfoService.updateById(activity);
        return Result.success("活动信息更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        ActivityInfo activity = activityInfoService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        activityInfoService.removeById(id);
        return Result.success("活动已删除");
    }
}
