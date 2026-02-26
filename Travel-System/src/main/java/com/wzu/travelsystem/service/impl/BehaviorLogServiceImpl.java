package com.wzu.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.BehaviorLog;
import com.wzu.travelsystem.mapper.BehaviorLogMapper;
import com.wzu.travelsystem.service.IBehaviorLogService;
import org.springframework.stereotype.Service;

@Service
public class BehaviorLogServiceImpl extends ServiceImpl<BehaviorLogMapper, BehaviorLog> implements IBehaviorLogService {
}