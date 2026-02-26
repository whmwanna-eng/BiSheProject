package com.wzu.travelsystem.service; // 修改这里

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzu.travelsystem.entity.User;

public interface IUserService extends IService<User> {
    User login(User user);
}