package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.ClubMember;

import java.util.List;

public interface ClubMemberService extends IService<ClubMember> {

    boolean applyJoin(Integer clubId, String studentId);

    boolean approveJoin(Integer id);

    boolean quitClub(Integer clubId, String studentId);

    List<ClubMember> listByClub(Integer clubId);

    boolean isClubLeader(Integer clubId, String studentId);
}
