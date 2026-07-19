package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.ActivityInfo;

import java.util.List;

public interface ActivityInfoService extends IService<ActivityInfo> {

    boolean applyActivity(ActivityInfo activity);

    boolean approveActivity(Integer activityId);

    boolean rejectActivity(Integer activityId);

    boolean finishActivity(Integer activityId);

    List<ActivityInfo> listPublished();

    List<ActivityInfo> listPending();

    List<ActivityInfo> listByClub(Integer clubId);
}
