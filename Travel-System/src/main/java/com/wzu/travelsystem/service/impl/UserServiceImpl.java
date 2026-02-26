package com.wzu.travelsystem.service.impl; // 修改这里

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzu.travelsystem.entity.User;
import com.wzu.travelsystem.mapper.UserMapper;
import com.wzu.travelsystem.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public User login(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 强制对传入的参数去空格
        wrapper.eq(User::getUsername, user.getUsername().trim());
        wrapper.eq(User::getPassword, user.getPassword().trim());
        return this.getOne(wrapper);
    }
}