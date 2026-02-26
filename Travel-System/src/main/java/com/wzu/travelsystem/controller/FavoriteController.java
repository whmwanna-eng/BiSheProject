package com.wzu.travelsystem.controller;

import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.Favorite;
import com.wzu.travelsystem.service.IFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    /**
     * 收藏或取消收藏接口
     */
    @PostMapping("/toggle")
    public Result toggle(@RequestBody Favorite favorite) {
        if (favorite.getUserId() == null || favorite.getAttractionId() == null) {
            return Result.error("用户ID或景点ID不能为空");
        }

        String message = favoriteService.toggleFavorite(favorite);
        return Result.success(message);
    }
}