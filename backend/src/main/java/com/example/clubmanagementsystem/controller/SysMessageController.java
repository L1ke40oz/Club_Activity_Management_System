package com.example.clubmanagementsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.SysMessage;
import com.example.clubmanagementsystem.entity.SysUser;
import com.example.clubmanagementsystem.service.SysMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class SysMessageController {

    @Autowired
    private SysMessageService messageService;

    @GetMapping("/my")
    public Result<List<SysMessage>> myMessages(HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", currentUser.getStudentId())
               .orderByDesc("create_time");
        return Result.success(messageService.list(wrapper));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", currentUser.getStudentId())
               .eq("is_read", 0);
        return Result.success(messageService.count(wrapper));
    }

    @PutMapping("/read/{id}")
    public Result<String> markRead(@PathVariable Integer id) {
        SysMessage msg = messageService.getById(id);
        if (msg != null) {
            msg.setIsRead(1);
            messageService.updateById(msg);
        }
        return Result.success("已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllRead(HttpSession session) {
        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", currentUser.getStudentId())
               .eq("is_read", 0);
        List<SysMessage> unread = messageService.list(wrapper);
        for (SysMessage msg : unread) {
            msg.setIsRead(1);
            messageService.updateById(msg);
        }
        return Result.success("全部已读");
    }
}
