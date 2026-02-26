package com.wzu.travelsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;       // 哪个用户打的分
    private Long attractionId; // 给哪个景点打的分
    private Double score;      // 分值 (1.0 - 5.0)
    private LocalDateTime createTime;
}