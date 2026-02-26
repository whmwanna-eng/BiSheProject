package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.Comment;
import com.wzu.travelsystem.entity.User;
import com.wzu.travelsystem.mapper.CommentMapper;
import com.wzu.travelsystem.mapper.UserMapper;
import com.wzu.travelsystem.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Comment> getCommentsByAttraction(Long attractionId) {
        // 1. 查询该景点的所有评论，按时间倒序
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<>();
        qw.eq(Comment::getAttractionId, attractionId).orderByDesc(Comment::getCreateTime);
        List<Comment> comments = this.list(qw);

        // 2. 遍历评论，补全用户名（用于前端回显）
        for (Comment c : comments) {
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                c.setUsername(user.getUsername());
            } else {
                c.setUsername("游客用户");
            }
        }
        return comments;
    }
}