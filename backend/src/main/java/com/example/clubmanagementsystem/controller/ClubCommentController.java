package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.ClubComment;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.ClubCommentService;
import com.example.clubmanagementsystem.service.SysUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/club-comment")
public class ClubCommentController {

    @Autowired
    private ClubCommentService clubCommentService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list/{clubId}")
    public Result<List<ClubComment>> listByClub(@PathVariable Integer clubId) {
        QueryWrapper<ClubComment> wrapper = new QueryWrapper<>();
        wrapper.eq("club_id", clubId).orderByDesc("create_time");
        List<ClubComment> list = clubCommentService.list(wrapper);
        // 补充学生姓名
        for (ClubComment comment : list) {
            SysUser user = sysUserService.getById(comment.getStudentId());
            if (user != null) {
                comment.setStudentName(user.getName());
            }
        }
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<String> addComment(@RequestBody ClubComment comment, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        comment.setStudentId(currentUser.getStudentId());
        comment.setCreateTime(LocalDateTime.now());
        if (comment.getRating() == null || comment.getRating() < 1 || comment.getRating() > 5) {
            comment.setRating(5);
        }
        clubCommentService.save(comment);
        return Result.success("评价成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Integer id, HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        ClubComment comment = clubCommentService.getById(id);
        if (comment == null) {
            return Result.error(404, "评价不存在");
        }
        if (!comment.getStudentId().equals(currentUser.getStudentId()) && currentUser.getRole() != 1) {
            return Result.error(403, "只能删除自己的评价");
        }
        clubCommentService.removeById(id);
        return Result.success("删除成功");
    }
}
