package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.ClubComment;
import com.example.clubmanagementsystem.mapper.ClubCommentMapper;
import com.example.clubmanagementsystem.service.ClubCommentService;
import org.springframework.stereotype.Service;

@Service
public class ClubCommentServiceImpl extends ServiceImpl<ClubCommentMapper, ClubComment> implements ClubCommentService {
}
