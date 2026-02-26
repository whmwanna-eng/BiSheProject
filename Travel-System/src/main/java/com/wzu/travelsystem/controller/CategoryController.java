package com.wzu.travelsystem.controller;

import com.wzu.travelsystem.common.Result;
import com.wzu.travelsystem.entity.Category;
import com.wzu.travelsystem.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    // 获取所有分类
    @GetMapping("/list")
    public Result list() {
        return Result.success(categoryService.list());
    }

    // 新增分类
    @PostMapping("/add")
    public Result add(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("分类添加成功");
    }

    // 删除分类
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        categoryService.removeById(id);
        return Result.success("分类已删除");
    }
}