package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.Attraction;
import com.wzu.travelsystem.mapper.AttractionMapper;
import com.wzu.travelsystem.service.IAttractionService;
import org.springframework.stereotype.Service;

@Service
public class AttractionServiceImpl extends ServiceImpl<AttractionMapper, Attraction> implements IAttractionService {
}