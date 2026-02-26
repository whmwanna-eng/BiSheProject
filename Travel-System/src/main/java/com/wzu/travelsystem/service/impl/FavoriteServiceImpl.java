package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.Favorite;
import com.wzu.travelsystem.mapper.FavoriteMapper;
import com.wzu.travelsystem.service.IFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Override
    public String toggleFavorite(Favorite favorite) {
        // 1. 构建查询条件，检查该用户是否已经收藏了该景点
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, favorite.getUserId())
                .eq(Favorite::getAttractionId, favorite.getAttractionId());

        Favorite existing = this.getOne(wrapper);

        if (existing != null) {
            // 2. 如果已存在，则删除（取消收藏）
            this.removeById(existing.getId());
            return "已取消收藏";
        } else {
            // 3. 如果不存在，则插入（收藏成功）
            this.save(favorite);
            return "收藏成功";
        }
    }
}