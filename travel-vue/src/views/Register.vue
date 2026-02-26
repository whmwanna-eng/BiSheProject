<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div
          style="
            text-align: center;
            font-weight: bold;
            font-size: 20px;
            color: #409eff;
          "
        >
          新用户注册
        </div>
      </template>

      <el-form :model="registerForm" label-position="top">
        <el-form-item label="用户名">
          <el-input
            v-model="registerForm.username"
            prefix-icon="User"
            placeholder="设置登录账号"
          />
        </el-form-item>

        <el-form-item label="设置密码">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            prefix-icon="Lock"
            placeholder="设置密码"
          />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
            v-model="registerForm.phone"
            prefix-icon="Iphone"
            placeholder="用于接收验证码"
          />
        </el-form-item>

        <el-form-item label="短信验证码">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input v-model="registerForm.code" placeholder="6位数字" />
            <el-button
              :disabled="isCountDown"
              type="success"
              @click="onTriggerVcode"
            >
              {{ isCountDown ? `${count}s后重发` : "获取验证码" }}
            </el-button>
          </div>
        </el-form-item>

        <el-button
          type="primary"
          style="width: 100%; margin-top: 10px"
          @click="handleRegister"
        >
          立即注册
        </el-button>

        <div style="margin-top: 20px; text-align: center">
          <el-link type="info" @click="router.push('/login')"
            >已有账号？返回登录</el-link
          >
        </div>
      </el-form>
    </el-card>

    <!-- 拼图验证码弹窗 -->
    <Vcode
      :show="isShowVcode"
      @success="onVcodeSuccess"
      @close="isShowVcode = false"
    />
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { ElMessage } from "element-plus";
import { User, Lock, Iphone } from "@element-plus/icons-vue";
// 引入拼图组件
import Vcode from "vue3-puzzle-vcode";

const router = useRouter();
const registerForm = ref({
  username: "",
  password: "",
  phone: "",
  code: "",
});

// --- 验证码逻辑 ---
const isShowVcode = ref(false); // 控制拼图显示
const isCountDown = ref(false); // 倒计时状态
const count = ref(60); // 倒数秒数

// 1. 点击“获取验证码”按钮，先触发拼图
const onTriggerVcode = () => {
  if (!/^1[3-9]\d{9}$/.test(registerForm.value.phone)) {
    return ElMessage.error("请输入正确的手机号");
  }
  isShowVcode.value = true;
};

// 2. 拼图成功后的回调
const onVcodeSuccess = async () => {
  isShowVcode.value = false; // 关闭拼图

  try {
    // 调用后端接口发送短信
    const res = await axios.get("http://localhost:8080/user/send-code", {
      params: { phone: registerForm.value.phone },
    });

    if (res.data.code === 200) {
      ElMessage.success("人机验证通过，验证码已发送（请查看控制台）");
      startCountDown(); // 开启倒计时
    }
  } catch (e) {
    ElMessage.error("发送失败");
  }
};

// 3. 倒计时逻辑
const startCountDown = () => {
  isCountDown.value = true;
  const timer = setInterval(() => {
    count.value--;
    if (count.value <= 0) {
      clearInterval(timer);
      isCountDown.value = false;
      count.value = 60;
    }
  }, 1000);
};

// --- 注册提交 ---
const handleRegister = async () => {
  if (!registerForm.value.code) return ElMessage.warning("请填写验证码");

  try {
    const res = await axios.post("http://localhost:8080/user/register", {
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone,
      code: registerForm.value.code,
    });

    if (res.data.code === 200) {
      ElMessage.success("注册成功，请登录");
      router.push("/login");
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    ElMessage.error("系统错误");
  }
};
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
.register-card {
  width: 400px;
  border-radius: 10px;
}
</style>
