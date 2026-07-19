package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.ActivityRegistration;
import com.example.clubmanagementsystem.mapper.ActivityRegistrationMapper;
import com.example.clubmanagementsystem.service.ActivityRegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> implements ActivityRegistrationService {

    @Override
    @Transactional
    public boolean signUp(Integer activityId, String studentId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId)
               .eq(ActivityRegistration::getStudentId, studentId);
        if (count(wrapper) > 0) {
            return false;
        }
        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(activityId);
        reg.setStudentId(studentId);
        reg.setRegTime(LocalDateTime.now());
        reg.setSignInStatus(0);
        return save(reg);
    }

    @Override
    public boolean signIn(Integer activityId, String studentId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId)
               .eq(ActivityRegistration::getStudentId, studentId);
        ActivityRegistration reg = getOne(wrapper);
        if (reg == null || reg.getSignInStatus() == 1) {
            return false;
        }
        reg.setSignInStatus(1);
        reg.setSignInTime(LocalDateTime.now());
        return updateById(reg);
    }

    @Override
    public List<ActivityRegistration> listByActivity(Integer activityId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId);
        return list(wrapper);
    }

    @Override
    public long countSignedIn(Integer activityId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId)
               .eq(ActivityRegistration::getSignInStatus, 1);
        return count(wrapper);
    }
}
