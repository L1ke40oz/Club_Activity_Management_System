package com.example.clubmanagementsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.clubmanagementsystem.entity.SysMessage;
import com.example.clubmanagementsystem.mapper.SysMessageMapper;
import com.example.clubmanagementsystem.service.SysMessageService;
import org.springframework.stereotype.Service;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {
}
