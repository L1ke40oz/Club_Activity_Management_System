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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/club")
public class ClubInfoController {

    @Autowired
    private ClubInfoService clubInfoService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private SysMessageService messageService;

    // 公开列表：只返回已通过审批的社团（status=1）
    @GetMapping("/list")
    public Result<List<ClubInfo>> list(@RequestParam(required = false) String name) {
        QueryWrapper<ClubInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0).eq("status", 1);
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        return Result.success(clubInfoService.list(wrapper));
    }

    // 管理员：获取待审批社团列表
    @GetMapping("/pending")
    public Result<List<ClubInfo>> pendingList() {
        QueryWrapper<ClubInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0).eq("status", 0);
        return Result.success(clubInfoService.list(wrapper));
    }

    // 我申请的社团（包括待审批和已驳回的）
    @GetMapping("/my-applied")
    public Result<List<ClubInfo>> myApplied(HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<ClubInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
               .eq("creator_id", currentUser.getStudentId())
               .in("status", 0, 2);
        return Result.success(clubInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<ClubInfo> getById(@PathVariable Integer id) {
        ClubInfo club = clubInfoService.getById(id);
        if (club == null || club.getIsDeleted() == 1) {
            return Result.error(404, "社团不存在");
        }
        return Result.success(club);
    }

    // 公开接口：供主页社团详情页使用
    @GetMapping("/detail/{id}")
    public Result<ClubInfo> publicDetail(@PathVariable Integer id) {
        ClubInfo club = clubInfoService.getById(id);
        if (club == null || club.getIsDeleted() == 1) {
            return Result.error(404, "社团不存在");
        }
        return Result.success(club);
    }

    // 学生申请创建社团（status=0 待审批）
    @PostMapping("/apply")
    public Result<String> applyClub(@RequestBody ClubInfo club, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        club.setCreatorId(currentUser.getStudentId());
        club.setStatus(0); // 待审批
        club.setIsDeleted(0);
        if (club.getMaxMembers() == null) {
            club.setMaxMembers(200);
        }
        clubInfoService.save(club);
        return Result.success("社团申请已提交，等待管理员审批");
    }

    // 管理员直接创建社团（status=1 直接通过）
    @PostMapping("/add")
    public Result<String> add(@RequestBody ClubInfo club, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        club.setCreatorId(currentUser.getStudentId());
        club.setStatus(1); // 直接通过
        club.setIsDeleted(0);
        if (club.getMaxMembers() == null) {
            club.setMaxMembers(200);
        }
        clubInfoService.save(club);
        return Result.success("社团创建成功");
    }

    // 管理员审批通过社团
    @PutMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Integer id) {
        ClubInfo club = clubInfoService.getById(id);
        if (club == null) {
            return Result.error(404, "社团不存在");
        }
        club.setStatus(1);
        clubInfoService.updateById(club);

        // 创建者自动成为社长
        ClubMember member = new ClubMember();
        member.setClubId(club.getClubId());
        member.setStudentId(club.getCreatorId());
        member.setPosition("社长");
        member.setStatus(1);
        member.setJoinTime(LocalDateTime.now());
        clubMemberService.save(member);

        // 发送站内信
        SysMessage msg = new SysMessage();
        msg.setReceiverId(club.getCreatorId());
        msg.setTitle("社团创建通知");
        msg.setContent("恭喜！您申请创建的社团「" + club.getName() + "」已通过审批，正式成立！您已被设为社长。");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageService.save(msg);

        return Result.success("社团审批通过");
    }

    // 管理员驳回社团申请
    @PutMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Integer id) {
        ClubInfo club = clubInfoService.getById(id);
        if (club == null) {
            return Result.error(404, "社团不存在");
        }
        club.setStatus(2);
        clubInfoService.updateById(club);

        // 发送站内信
        SysMessage msg = new SysMessage();
        msg.setReceiverId(club.getCreatorId());
        msg.setTitle("社团申请通知");
        msg.setContent("很抱歉，您申请创建的社团「" + club.getName() + "」未通过审批。请修改后重新申请。");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageService.save(msg);

        return Result.success("已驳回");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ClubInfo club) {
        clubInfoService.updateById(club);
        return Result.success("社团信息更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        ClubInfo club = clubInfoService.getById(id);
        if (club == null) {
            return Result.error(404, "社团不存在");
        }
        clubInfoService.removeById(id);
        return Result.success("社团已解散");
    }
}
