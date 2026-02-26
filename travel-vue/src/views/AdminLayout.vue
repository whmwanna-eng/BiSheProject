<template>
  <div class="admin-container">
    <el-container style="height: 100vh">
      <el-aside width="220px" style="background-color: #304156">
        <div
          style="
            height: 60px;
            line-height: 60px;
            color: white;
            text-align: center;
            font-weight: bold;
            background: #2b2f3a;
          "
        >
          旅游系统管理后台
        </div>
        <el-menu
          active-text-color="#ffd04b"
          background-color="#304156"
          class="el-menu-vertical"
          :default-active="route.path"
          text-color="#fff"
          router
        >
          <el-menu-item index="/admin/attraction">
            <el-icon><Location /></el-icon>
            <span>景点信息维护</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <el-icon><Menu /></el-icon>
            <span>分类标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><User /></el-icon>
            <span>用户权限控制</span>
          </el-menu-item>
          <el-divider />
          <el-menu-item index="/home">
            <el-icon><Back /></el-icon>
            <span>返回前台首页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header
          style="
            border-bottom: 1px solid #ddd;
            display: flex;
            align-items: center;
            justify-content: space-between;
          "
        >
          <span style="font-weight: bold">管理员控制台</span>
          <el-tag type="danger">管理员身份</el-tag>
        </el-header>

        <el-main>
          <!-- 【核心】没有这一行，子页面（景点维护、用户管理）无法显示 -->
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute } from "vue-router";
const route = useRoute();
</script>
