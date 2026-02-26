package com.wzu.travelsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzu.travelsystem.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}