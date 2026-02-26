package com.wzu.travelsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long attractionId;
    private String content;
    private LocalDateTime createTime;

    // 重点：这个字段数据库里没有，仅用于前端展示
    @TableField(exist = false)
    private String username;
}