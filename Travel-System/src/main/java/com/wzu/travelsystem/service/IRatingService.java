package com.wzu.travelsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzu.travelsystem.entity.Rating;

public interface IRatingService extends IService<Rating> {
    // 提交评分逻辑（如果已存在则更新，不存在则新增）
    void saveOrUpdateRating(Rating rating);
}