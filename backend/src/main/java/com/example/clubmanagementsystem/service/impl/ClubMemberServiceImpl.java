package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.ClubMember;
import com.example.clubmanagementsystem.mapper.ClubMemberMapper;
import com.example.clubmanagementsystem.service.ClubMemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClubMemberServiceImpl extends ServiceImpl<ClubMemberMapper, ClubMember> implements ClubMemberService {

    @Override
    public boolean applyJoin(Integer clubId, String studentId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getStudentId, studentId)
               .ne(ClubMember::getStatus, 2);
        if (count(wrapper) > 0) {
            return false;
        }
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setStudentId(studentId);
        member.setPosition("干事");
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(0);
        return save(member);
    }

    @Override
    public boolean approveJoin(Integer id) {
        ClubMember member = getById(id);
        if (member == null || member.getStatus() != 0) {
            return false;
        }
        member.setStatus(1);
        return updateById(member);
    }

    @Override
    public boolean quitClub(Integer clubId, String studentId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getStudentId, studentId)
               .eq(ClubMember::getStatus, 1);
        ClubMember member = getOne(wrapper);
        if (member == null) {
            return false;
        }
        member.setStatus(2);
        return updateById(member);
    }

    @Override
    public List<ClubMember> listByClub(Integer clubId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public boolean isClubLeader(Integer clubId, String studentId) {
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
               .eq(ClubMember::getStudentId, studentId)
               .eq(ClubMember::getStatus, 1)
               .eq(ClubMember::getPosition, "社长");
        return count(wrapper) > 0;
    }
}
