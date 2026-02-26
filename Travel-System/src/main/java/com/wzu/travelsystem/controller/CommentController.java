package com.wzu.travelsystem.controller;

import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.Comment;
import com.wzu.travelsystem.service.ICommentService; // 引入接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService; // 注入接口

    @GetMapping("/list")
    public Result list(@RequestParam Long attractionId) {
        return Result.success(commentService.getCommentsByAttraction(attractionId));
    }

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        comment.setCreateTime(LocalDateTime.now());
        commentService.save(comment);
        return Result.success("评论发表成功");
    }
}