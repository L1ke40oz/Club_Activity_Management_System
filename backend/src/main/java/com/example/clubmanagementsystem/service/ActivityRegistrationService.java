package com.example.clubmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.clubmanagementsystem.entity.ActivityRegistration;

import java.util.List;

public interface ActivityRegistrationService extends IService<ActivityRegistration> {

    boolean signUp(Integer activityId, String studentId);

    boolean signIn(Integer activityId, String studentId);

    List<ActivityRegistration> listByActivity(Integer activityId);

    long countSignedIn(Integer activityId);
}
