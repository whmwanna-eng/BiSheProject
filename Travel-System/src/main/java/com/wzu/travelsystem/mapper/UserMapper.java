package com.wzu.travelsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzu.travelsystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}