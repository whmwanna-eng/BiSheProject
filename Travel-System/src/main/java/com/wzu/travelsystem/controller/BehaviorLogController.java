package com.wzu.travelsystem.controller;

import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.BehaviorLog;
import com.wzu.travelsystem.service.IBehaviorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/behavior")
public class BehaviorLogController {

    @Autowired
    private IBehaviorLogService behaviorLogService;

    /**
     * 接收前端上报的用户行为数据
     * 场景：
     * 1. 用户点击进入景点详情页 -> actionType = "CLICK", clickCount = 1
     * 2. 用户离开景点详情页 -> actionType = "BROWSE", browseDuration = xx秒
     * 3. 用户在首页搜索栏搜索 -> actionType = "SEARCH", searchKeyword = "xxx"
     */
    @PostMapping("/report")
    public Result reportBehavior(@RequestBody BehaviorLog log) {
        // 基本校验：如果没有登录（缺少 userId），则不记录行为
        if (log.getUserId() == null) {
            return Result.error("用户未登录，不记录行为数据");
        }

        // 设置创建时间
        log.setCreateTime(LocalDateTime.now());

        // 将数据保存到数据库，供 RecommendationService 分析使用
        boolean saved = behaviorLogService.save(log);

        if (saved) {
            return Result.success("行为数据上报成功");
        } else {
            return Result.error("行为数据上报失败");
        }
    }
}