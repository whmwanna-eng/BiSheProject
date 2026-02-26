package com.wzu.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.User;
import com.wzu.travelsystem.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "用户模块", description = "处理用户的注册、登录、短信验证及个人信息管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Operation(summary = "发送短信验证码", description = "生成6位验证码并存入Redis，有效期5分钟")
    @GetMapping("/send-code")
    public Result sendCode(@RequestParam String phone) {
        // 1. 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 2. 存入 Redis，Key增加前缀防止冲突，设置5分钟过期
        redisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);

        // 3. 模拟发送：控制台打印 (你在IDEA控制台查看此验证码)
        System.out.println("【智慧旅游】您的短信验证码为：" + code + " (有效期5分钟)");

        return Result.success("验证码已发送至控制台（模拟）");
    }

    @Operation(summary = "用户注册", description = "包含短信验证码校验，成功后存入手机号等信息")
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");
        String code = params.get("code");

        // 1. 验证码校验
        String redisCode = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (redisCode == null || !redisCode.equals(code)) {
            return Result.error("验证码错误或已过期");
        }

        // 2. 校验用户名是否重复
        if (userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            return Result.error("用户名已存在");
        }

        // 3. 创建并保存用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际项目建议加密
        user.setPhone(phone);
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());

        boolean saved = userService.save(user);
        if (saved) {
            redisTemplate.delete("sms:code:" + phone); // 注册成功删除验证码
            return Result.success("注册成功");
        }
        return Result.error("注册失败，请稍后再试");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userService.login(user);
        if (loginUser != null) {
            loginUser.setPassword(null); // 隐藏密码
            return Result.success(loginUser);
        }
        return Result.error("用户名或密码错误");
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        if (user.getId() == null) return Result.error("用户ID不能为空");

        // 1. 执行更新
        boolean success = userService.updateById(user);
        if (success) {
            // 2. 获取更新后的最新对象（去掉密码）返回给前端
            User newUser = userService.getById(user.getId());
            newUser.setPassword(null);
            return Result.success(newUser);
        }
        return Result.error("资料更新失败");
    }

    @Operation(summary = "获取所有用户列表", description = "管理员后台使用")
    @GetMapping("/list")
    public Result listAll() {
        return Result.success(userService.list());
    }

    @Operation(summary = "修改用户角色", description = "权限控制核心")
    @PutMapping("/role")
    public Result updateRole(@RequestBody User user) {
        userService.updateById(user);
        return Result.success("权限调整成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("用户已移除");
    }
}