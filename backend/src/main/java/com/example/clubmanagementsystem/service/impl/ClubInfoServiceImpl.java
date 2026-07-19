package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.ClubInfo;
import com.example.clubmanagementsystem.mapper.ClubInfoMapper;
import com.example.clubmanagementsystem.service.ClubInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubInfoServiceImpl extends ServiceImpl<ClubInfoMapper, ClubInfo> implements ClubInfoService {

    @Override
    public List<ClubInfo> listActive() {
        LambdaQueryWrapper<ClubInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubInfo::getIsDeleted, 0);
        return list(wrapper);
    }

    @Override
    public boolean dissolveClub(Integer clubId) {
        ClubInfo club = getById(clubId);
        if (club == null || club.getIsDeleted() == 1) {
            return false;
        }
        club.setIsDeleted(1);
        return updateById(club);
    }
}
