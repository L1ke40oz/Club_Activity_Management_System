package com.example.clubmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.clubmanagementsystem.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {
}
