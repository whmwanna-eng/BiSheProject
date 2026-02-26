package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.Rating;
import com.wzu.travelsystem.mapper.RatingMapper;
import com.wzu.travelsystem.service.IRatingService;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements IRatingService {
    @Override
    public void saveOrUpdateRating(Rating rating) {
        LambdaQueryWrapper<Rating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Rating::getUserId, rating.getUserId())
                .eq(Rating::getAttractionId, rating.getAttractionId());

        Rating existing = this.getOne(wrapper);

        if (existing != null) {
            // 2. 如果存在记录，把旧的主键 ID 赋给新对象，触发 MyBatis-Plus 的更新机制
            existing.setScore(rating.getScore());
            existing.setCreateTime(java.time.LocalDateTime.now()); // 更新时间
            this.updateById(existing);
            System.out.println("检测到已有评分，执行了 UPDATE 操作");
        } else {
            // 3. 如果不存在，执行插入
            this.save(rating);
            System.out.println("未检测到评分，执行了 INSERT 操作");
        }
    }
}