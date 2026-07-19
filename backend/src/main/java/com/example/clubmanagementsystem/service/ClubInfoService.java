package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.ClubInfo;

import java.util.List;

public interface ClubInfoService extends IService<ClubInfo> {

    List<ClubInfo> listActive();

    boolean dissolveClub(Integer clubId);
}
