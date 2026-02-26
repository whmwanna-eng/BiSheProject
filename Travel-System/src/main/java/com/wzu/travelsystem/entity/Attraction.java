package com.wzu.travelsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("attraction")
@Schema(description = "景点信息实体")
public class Attraction {
    @TableId(type = IdType.AUTO)
    @Schema(description = "景点ID")
    private Long id;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "所属城市")
    private String city;

    @Schema(description = "景点描述")
    private String description;

    @Schema(description = "图片链接")
    private String imageUrl;

    @Schema(description = "门票价格")
    private BigDecimal price;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}