package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.ClubInfo;
import com.example.clubmanagementsystem.entity.ClubMember;
import com.example.clubmanagementsystem.entity.SysMessage;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.ClubInfoService;
import com.example.clubmanagementsystem.service.ClubMemberService;
import com.example.clubmanagementsystem.service.SysMessageService;
import com.example.clubmanagementsystem.service.SysUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/club-member")
public class ClubMemberController {

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private ClubInfoService clubInfoService;

    @Autowired
    private SysMessageService messageService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list/{clubId}")
    public Result<List<ClubMember>> listByClub(@PathVariable Integer clubId,
                                               @RequestParam(required = false) Integer status) {
        QueryWrapper<ClubMember> wrapper = new QueryWrapper<>();
        wrapper.eq("club_id", clubId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        List<ClubMember> members = clubMemberService.list(wrapper);
        enrichMemberInfo(members);
        return Result.success(members);
    }

    // 管理员：获取全部待审批成员申请
    @GetMapping("/pending-all")
    public Result<List<ClubMember>> allPending() {
        QueryWrapper<ClubMember> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<ClubMember> members = clubMemberService.list(wrapper);
        enrichMemberInfo(members);
        return Result.success(members);
    }

    private void enrichMemberInfo(List<ClubMember> members) {
        for (ClubMember member : members) {
            SysUser user = sysUserService.getById(member.getStudentId());
            if (user != null) {
                member.setName(user.getName());
                member.setClassName(user.getClassName());
            }
        }
    }

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody ClubMember member, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        member.setStudentId(currentUser.getStudentId());

        // 检查是否已申请或已加入（status=0待审批 或 status=1已通过）
        QueryWrapper<ClubMember> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("club_id", member.getClubId())
               .eq("student_id", member.getStudentId())
               .in("status", 0, 1);
        if (clubMemberService.count(activeWrapper) > 0) {
            return Result.error(400, "已申请或已加入该社团");
        }

        // 检查社团人数上限
        ClubInfo club = clubInfoService.getById(member.getClubId());
        if (club != null && club.getMaxMembers() != null) {
            QueryWrapper<ClubMember> countWrapper = new QueryWrapper<>();
            countWrapper.eq("club_id", member.getClubId()).eq("status", 1);
            long currentCount = clubMemberService.count(countWrapper);
            if (currentCount >= club.getMaxMembers()) {
                return Result.error(400, "该社团成员已满，无法申请加入");
            }
        }

        // 检查是否有历史记录（之前退出过），有则更新，无则新增
        QueryWrapper<ClubMember> existWrapper = new QueryWrapper<>();
        existWrapper.eq("club_id", member.getClubId())
                    .eq("student_id", member.getStudentId());
        ClubMember existing = clubMemberService.getOne(existWrapper);
        if (existing != null) {
            existing.setStatus(0);
            existing.setPosition("成员");
            existing.setJoinTime(LocalDateTime.now());
            clubMemberService.updateById(existing);
        } else {
            member.setStatus(0);
            member.setJoinTime(LocalDateTime.now());
            clubMemberService.save(member);
        }

        return Result.success("申请已提交");
    }

    @PutMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Integer id) {
        ClubMember member = clubMemberService.getById(id);
        if (member == null) {
            return Result.error(404, "记录不存在");
        }

        // 审批前再次检查人数上限
        ClubInfo club = clubInfoService.getById(member.getClubId());
        if (club != null && club.getMaxMembers() != null) {
            QueryWrapper<ClubMember> countWrapper = new QueryWrapper<>();
            countWrapper.eq("club_id", member.getClubId()).eq("status", 1);
            long currentCount = clubMemberService.count(countWrapper);
            if (currentCount >= club.getMaxMembers()) {
                return Result.error(400, "社团成员已满，无法通过审批");
            }
        }

        member.setStatus(1);
        clubMemberService.updateById(member);

        // 发送站内信通知用户
        String clubName = club != null ? club.getName() : "社团";
        SysMessage msg = new SysMessage();
        msg.setReceiverId(member.getStudentId());
        msg.setTitle("社团加入通知");
        msg.setContent("恭喜！您申请加入「" + clubName + "」已通过审批，欢迎成为社团的一员！");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageService.save(msg);

        return Result.success("审批通过");
    }

    @PutMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Integer id) {
        ClubMember member = clubMemberService.getById(id);
        if (member == null) {
            return Result.error(404, "记录不存在");
        }

        // 获取社团名称
        ClubInfo club = clubInfoService.getById(member.getClubId());
        String clubName = club != null ? club.getName() : "社团";

        member.setStatus(2);
        clubMemberService.updateById(member);

        // 发送站内信通知用户
        SysMessage msg = new SysMessage();
        msg.setReceiverId(member.getStudentId());
        msg.setTitle("社团申请通知");
        msg.setContent("很抱歉，您申请加入「" + clubName + "」未通过审批。您可以尝试加入其他感兴趣的社团。");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageService.save(msg);

        return Result.success("已拒绝");
    }

    @PutMapping("/quit/{clubId}")
    public Result<String> quit(@PathVariable Integer clubId, @RequestParam(required = false) String transferTo, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<ClubMember> wrapper = new QueryWrapper<>();
        wrapper.eq("club_id", clubId)
               .eq("student_id", currentUser.getStudentId())
               .eq("status", 1);
        ClubMember member = clubMemberService.getOne(wrapper);
        if (member == null) {
            return Result.error(400, "你不是该社团成员");
        }

        boolean isLeader = "社长".equals(member.getPosition()) || "队长".equals(member.getPosition());

        if (isLeader) {
            // 查看社团是否还有其他成员
            QueryWrapper<ClubMember> otherWrapper = new QueryWrapper<>();
            otherWrapper.eq("club_id", clubId).eq("status", 1).ne("student_id", currentUser.getStudentId());
            List<ClubMember> others = clubMemberService.list(otherWrapper);

            if (others.isEmpty()) {
                // 没有其他成员，直接解散社团
                member.setStatus(2);
                clubMemberService.updateById(member);
                ClubInfo club = clubInfoService.getById(clubId);
                if (club != null) {
                    clubInfoService.removeById(clubId);
                }
                return Result.success("社团已无其他成员，社团已解散");
            } else if (transferTo != null && !transferTo.isEmpty()) {
                // 转让社长给指定成员
                ClubMember newLeader = others.stream()
                        .filter(m -> m.getStudentId().equals(transferTo))
                        .findFirst().orElse(null);
                if (newLeader == null) {
                    return Result.error(400, "指定的转让对象不是社团成员");
                }
                newLeader.setPosition("社长");
                clubMemberService.updateById(newLeader);
                member.setStatus(2);
                clubMemberService.updateById(member);

                // 通知新社长
                ClubInfo club = clubInfoService.getById(clubId);
                SysMessage msg = new SysMessage();
                msg.setReceiverId(transferTo);
                msg.setTitle("社长转让通知");
                msg.setContent("您已被任命为「" + (club != null ? club.getName() : "社团") + "」的新社长！");
                msg.setIsRead(0);
                msg.setCreateTime(LocalDateTime.now());
                messageService.save(msg);

                return Result.success("已转让社长并退出社团");
            } else {
                // 社长退出但没指定接任者，返回提示
                return Result.error(400, "您是社长，请选择转让社长给其他成员或解散社团");
            }
        }

        // 普通成员直接退出
        member.setStatus(2);
        clubMemberService.updateById(member);
        return Result.success("退出成功");
    }

    @PutMapping("/position")
    public Result<String> updatePosition(@RequestBody ClubMember member) {
        ClubMember existing = clubMemberService.getById(member.getId());
        if (existing == null) {
            return Result.error(404, "记录不存在");
        }
        existing.setPosition(member.getPosition());
        clubMemberService.updateById(existing);
        return Result.success("职务更新成功");
    }
}
