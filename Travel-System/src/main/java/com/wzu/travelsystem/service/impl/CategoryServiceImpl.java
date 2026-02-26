package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.Category;
import com.wzu.travelsystem.mapper.CategoryMapper;
import com.wzu.travelsystem.service.ICategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
}