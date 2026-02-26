<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>旅游系统 - 用户登录</h2>
      <el-form :model="loginForm">
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="handleLogin"
          >登 录</el-button
        >

        <!-- 新增：注册跳转链接 -->
        <div style="margin-top: 20px; text-align: center; font-size: 13px">
          没有账号？<el-link type="primary" @click="router.push('/register')"
            >立即注册</el-link
          >
        </div>

        <div
          style="
            margin-top: 15px;
            text-align: center;
            font-size: 12px;
            color: #999;
          "
        >
          测试账号：user_1 密码：123
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";

const router = useRouter();
const loginForm = ref({ username: "", password: "" });

const handleLogin = async () => {
  // 基础判空
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning("请输入用户名和密码");
    return;
  }

  // 关键修复：执行 .trim() 去除可能的不可见空格
  const submitData = {
    username: loginForm.value.username.trim(),
    password: loginForm.value.password.trim(),
  };

  try {
    const res = await axios.post(
      "http://localhost:8080/user/login",
      submitData,
    );
    if (res.data.code === 200) {
      const user = res.data.data;
      // 将用户信息存入本地缓存
      localStorage.setItem("user", JSON.stringify(user));

      // 根据角色进行提示分流
      if (user.role === "ADMIN") {
        ElMessage.info("管理员您好，为您跳转至门户首页");
      } else {
        ElMessage.success("欢迎进入智慧旅游系统");
      }

      router.push("/home"); // 登录成功跳转首页
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("服务连接失败，请检查后端是否启动");
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 10px;
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #409eff;
}
</style>
