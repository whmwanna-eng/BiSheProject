package com.wzu.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.Attraction;
import com.wzu.travelsystem.service.IAttractionService;
import com.wzu.travelsystem.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 景点信息管理核心控制器
 * 包含：基础分页、多条件搜索、双引擎智慧推荐
 */
@RestController
@RequestMapping("/attraction")
public class AttractionController {

    @Autowired
    private IAttractionService attractionService;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 1. 景点分页查询与搜索接口
     * 支持功能：
     * - 默认展示列表
     * - 按名称模糊搜索 (name)
     * - 按城市精确筛选 (city)
     * - 自动分页
     *
     * 测试地址：http://localhost:8080/attraction/page?current=1&size=10&city=温州
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Integer current,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String name) { // 建议只接收一个 keyword 参数

        Page<Attraction> page = new Page<>(current, size);
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();

        // 关键修改：使用 .and(...) 嵌套一个 (A OR B) 逻辑
        if (name != null && !name.isEmpty()) {
            wrapper.and(w -> w.like(Attraction::getName, name)
                    .or()
                    .like(Attraction::getCity, name));
        }

        wrapper.orderByDesc(Attraction::getId);
        return Result.success(attractionService.page(page, wrapper));
    }

    /**
     * 2. 智慧推荐接口 (双算法引擎)
     *
     * @param userId 当前用户ID (必填，用于计算偏好)
     * @param type   推荐类型：
     *               - "user": 基于用户的协同过滤 (User-CF)，寻找口味相似的驴友
     *               - "item": 对齐开题报告表2的混合相似度推荐，基于分类、位置和群体重叠度
     *
     * 测试地址：http://localhost:8080/attraction/recommend?userId=1&type=item
     */
    @GetMapping("/recommend")
    public Result getRecommend(@RequestParam Long userId,
                               @RequestParam(defaultValue = "user") String type) {

        List<Long> recommendIds;

        // 根据前端参数切换算法逻辑
        if ("item".equals(type)) {
            // 执行对齐表2的混合特征推荐
            recommendIds = recommendationService.suggestByHybridItemCF(userId, 4);
        } else {
            // 执行基础的用户协同过滤推荐
            recommendIds = recommendationService.suggestByUserCF(userId, 4);
        }

        // 结果处理：如果算法没有计算出结果（冷启动或新用户数据不足）
        if (recommendIds == null || recommendIds.isEmpty()) {
            // 兜底策略：返回系统中最新的4个景点作为推荐
            LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Attraction::getId).last("LIMIT 4");
            List<Attraction> fallbackList = attractionService.list(wrapper);
            return Result.success(fallbackList);
        }

        // 根据ID列表批量获取景点详情信息并返回
        List<Attraction> recommendList = attractionService.listByIds(recommendIds);
        return Result.success(recommendList);
    }

    /**
     * 3. 景点详情获取
     * 点击单个景点卡片时调用
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Attraction attraction = attractionService.getById(id);
        if (attraction == null) {
            return Result.error("该景点不存在或已被删除");
        }
        return Result.success(attraction);
    }


    /**
     * 这个接口的作用：
     * 给它一个 userId 和 attractionId，它告诉你用户是否收藏过、打了多少分
     */

    // 1. 确保顶部注入了这两个 Mapper
    @Autowired
    private com.wzu.travelsystem.mapper.RatingMapper ratingMapper;
    @Autowired
    private com.wzu.travelsystem.mapper.FavoriteMapper favoriteMapper;

    /**
     * 获取当前用户对特定景点的交互状态（用于前端回显）
     * 地址：GET /attraction/interaction/status
     */
    @GetMapping("/interaction/status")
    public com.wzu.travelsystem.common.Result getInteractionStatus(
            @RequestParam Long userId,
            @RequestParam Long attractionId) {

        Map<String, Object> resultMap = new HashMap<>();

        // 1. 查询评分状态
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wzu.travelsystem.entity.Rating> rWrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        rWrapper.eq(com.wzu.travelsystem.entity.Rating::getUserId, userId)
                .eq(com.wzu.travelsystem.entity.Rating::getAttractionId, attractionId);
        com.wzu.travelsystem.entity.Rating rating = ratingMapper.selectOne(rWrapper);

        // 如果没打过分，返回0
        resultMap.put("score", rating != null ? rating.getScore() : 0);

        // 2. 查询收藏状态
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wzu.travelsystem.entity.Favorite> fWrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        fWrapper.eq(com.wzu.travelsystem.entity.Favorite::getUserId, userId)
                .eq(com.wzu.travelsystem.entity.Favorite::getAttractionId, attractionId);
        Long favCount = favoriteMapper.selectCount(fWrapper);

        // 数量大于0说明已收藏
        resultMap.put("isFavorited", favCount > 0);

        return com.wzu.travelsystem.common.Result.success(resultMap);
    }


    /**
     * 获取指定用户收藏的所有景点
     * 地址：GET /attraction/my-favorites?userId=1
     */
    @GetMapping("/my-favorites")
    public Result getMyFavorites(@RequestParam Long userId) {
        // 1. 先从 favorite 表里找出该用户收藏的所有 attractionId
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wzu.travelsystem.entity.Favorite> fWrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        fWrapper.eq(com.wzu.travelsystem.entity.Favorite::getUserId, userId);

        List<com.wzu.travelsystem.entity.Favorite> favorites = favoriteMapper.selectList(fWrapper);

        // 2. 如果没收藏任何东西，直接返回空列表
        if (favorites.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 3. 提取 ID 列表并查询景点详情
        List<Long> ids = favorites.stream().map(com.wzu.travelsystem.entity.Favorite::getAttractionId).collect(Collectors.toList());
        List<com.wzu.travelsystem.entity.Attraction> list = attractionService.listByIds(ids);

        return Result.success(list);
    }

    /**
     * 管理员操作：新增、修改、删除
     *
     */
    // 1. 新增景点
    @PostMapping("/save")
    public Result save(@RequestBody Attraction attraction) {
        attraction.setCreateTime(java.time.LocalDateTime.now());
        attractionService.save(attraction);
        return Result.success("新增景点成功");
    }

    // 2. 修改景点
    @PutMapping("/update")
    public Result update(@RequestBody Attraction attraction) {
        attractionService.updateById(attraction);
        return Result.success("修改景点成功");
    }

    // 3. 删除景点
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        attractionService.removeById(id);
        return Result.success("景点已删除");
    }
}