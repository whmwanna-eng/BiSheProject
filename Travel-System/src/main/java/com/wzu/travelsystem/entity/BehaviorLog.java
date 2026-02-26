package com.wzu.travelsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("behavior_log")
public class BehaviorLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long attractionId;
    private Integer browseDuration; // 浏览时长(秒)
    private Integer clickCount;     // 点击次数
    private String searchKeyword;   // 搜索关键词
    private LocalDateTime createTime;
}