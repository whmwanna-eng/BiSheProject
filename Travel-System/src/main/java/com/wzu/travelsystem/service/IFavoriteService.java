package com.wzu.travelsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzu.travelsystem.entity.Favorite;

public interface IFavoriteService extends IService<Favorite> {
    /**
     * 切换收藏状态：已收藏则取消，未收藏则添加
     * @return 返回操作结果描述
     */
    String toggleFavorite(Favorite favorite);
}