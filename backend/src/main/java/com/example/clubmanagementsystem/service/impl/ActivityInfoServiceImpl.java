package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.ActivityInfo;
import com.example.clubmanagementsystem.mapper.ActivityInfoMapper;
import com.example.clubmanagementsystem.service.ActivityInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Override
    public boolean applyActivity(ActivityInfo activity) {
        activity.setStatus(0);
        activity.setIsDeleted(0);
        return save(activity);
    }

    @Override
    public boolean approveActivity(Integer activityId) {
        ActivityInfo activity = getById(activityId);
        if (activity == null || activity.getStatus() != 0) {
            return false;
        }
        activity.setStatus(1);
        return updateById(activity);
    }

    @Override
    public boolean rejectActivity(Integer activityId) {
        ActivityInfo activity = getById(activityId);
        if (activity == null || activity.getStatus() != 0) {
            return false;
        }
        activity.setStatus(3);
        return updateById(activity);
    }

    @Override
    public boolean finishActivity(Integer activityId) {
        ActivityInfo activity = getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return false;
        }
        activity.setStatus(2);
        return updateById(activity);
    }

    @Override
    public List<ActivityInfo> listPublished() {
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getStatus, 1)
               .eq(ActivityInfo::getIsDeleted, 0);
        return list(wrapper);
    }

    @Override
    public List<ActivityInfo> listPending() {
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getStatus, 0)
               .eq(ActivityInfo::getIsDeleted, 0);
        return list(wrapper);
    }

    @Override
    public List<ActivityInfo> listByClub(Integer clubId) {
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getClubId, clubId)
               .eq(ActivityInfo::getIsDeleted, 0);
        return list(wrapper);
    }
}
