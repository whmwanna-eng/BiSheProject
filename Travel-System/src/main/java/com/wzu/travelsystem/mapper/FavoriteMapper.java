package com.wzu.travelsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzu.travelsystem.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}