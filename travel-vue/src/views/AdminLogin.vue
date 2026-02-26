<template>
  <div class="admin-login-bg">
    <el-card class="admin-login-card">
      <template #header>
        <div
          style="
            text-align: center;
            font-weight: bold;
            font-size: 20px;
            color: #409eff;
          "
        >
          智慧旅游 - 管理员后台登录
        </div>
      </template>

      <el-form :model="loginForm" label-position="top">
        <el-form-item label="管理员账号">
          <el-input
            v-model="loginForm.username"
            prefix-icon="User"
            placeholder="请输入管理员用户名"
          />
        </el-form-item>
        <el-form-item label="安全密码">
          <el-input
            v-model="loginForm.password"
            prefix-icon="Lock"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>

        <el-button
          type="primary"
          style="width: 100%; height: 45px; font-size: 16px"
          @click="handleAdminLogin"
        >
          验证并进入系统后台
        </el-button>

        <div style="margin-top: 20px; text-align: right">
          <el-link type="info" @click="router.push('/login')"
            >前台门户登录</el-link
          >
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

const handleAdminLogin = async () => {
  try {
    const res = await axios.post(
      "http://localhost:8080/user/login",
      loginForm.value,
    );

    if (res.data.code === 200) {
      const user = res.data.data;

      // 核心权限检查：只有 ADMIN 角色才能从这里登录
      if (user.role === "ADMIN") {
        localStorage.setItem("user", JSON.stringify(user));
        ElMessage.success("身份验证成功，正在进入管理后台...");
        router.push("/admin"); // 登录成功直接跳转到后台页面
      } else {
        ElMessage.error("访问受限：您的账号不具备管理员权限！");
      }
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    ElMessage.error("后台系统未连接");
  }
};
</script>

<style scoped>
.admin-login-bg {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b; /* 使用暗色调突出管理系统的严肃感 */
}
.admin-login-card {
  width: 450px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}
</style>
