package com.wzu.travelsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzu.travelsystem.entity.Comment;
import java.util.List;

public interface ICommentService extends IService<Comment> {
    /**
     * 根据景点ID获取评论列表，并关联查询用户名
     */
    List<Comment> getCommentsByAttraction(Long attractionId);
}