package com.wzu.travelsystem.controller;

import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.Rating;
import com.wzu.travelsystem.service.IRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rating")
public class RatingController {

    @Autowired
    private IRatingService ratingService;

    // 提交评分
    @PostMapping("/submit")
    public Result submit(@RequestBody Rating rating) {

        System.out.println("收到前端数据 -> 用户ID: " + rating.getUserId() + ", 景点ID: " + rating.getAttractionId() + ", 分数: " + rating.getScore());

        if (rating.getUserId() == null || rating.getAttractionId() == null) {
            return Result.error("后端接收到的参数是空的！请检查实体类字段名");
        }


        if (rating.getScore() < 0 || rating.getScore() > 5) {
            return Result.error("评分必须在0-5分之间");
        }
        ratingService.saveOrUpdateRating(rating);
        return Result.success("评价成功");
    }
}