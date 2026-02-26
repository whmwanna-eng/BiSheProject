<template>
  <div class="profile-page">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center; gap: 10px">
            <el-button icon="Back" circle @click="router.push('/home')" />
            <span style="font-weight: bold">个人信息管理</span>
          </div>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="100px" label-position="left">
        <el-form-item label="登录账号">
          <el-input v-model="form.username" disabled />
          <p style="font-size: 12px; color: #999; margin: 5px 0">
            用户名不可修改
          </p>
        </el-form-item>

        <el-form-item label="我的昵称">
          <el-input v-model="form.nickname" placeholder="给起个好听的昵称吧" />
        </el-form-item>

        <el-form-item label="手机号码">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input
            v-model="form.profileCard"
            type="textarea"
            :rows="3"
            placeholder="介绍一下你自己，比如：资深驴友，爱看山水..."
          />
        </el-form-item>

        <el-divider>安全设置</el-divider>

        <el-form-item label="重置密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="如不修改请留空"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { ElMessage } from "element-plus";
import { Back } from "@element-plus/icons-vue";

const router = useRouter();

// 1. 初始化：从缓存获取当前登录的用户信息
const userStr = localStorage.getItem("user");
const currentUser = userStr ? JSON.parse(userStr) : {};

// 2. 将数据绑定到表单（解构一下，避免修改时直接影响缓存，等点击保存再更新）
const form = ref({
  id: currentUser.id,
  username: currentUser.username,
  nickname: currentUser.nickname || "",
  phone: currentUser.phone || "",
  profileCard: currentUser.profileCard || "",
  password: "", // 密码默认留空
});

// 3. 执行保存逻辑
const handleSave = async () => {
  try {
    const submitData = { ...form.value };
    if (!submitData.password) delete submitData.password;

    const res = await axios.put(
      "http://localhost:8080/user/update",
      submitData,
    );

    if (res.data.code === 200) {
      ElMessage.success("个人资料已更新");

      // 【关键点】把后端更新后的完整用户对象（包含新昵称）覆盖掉旧的缓存
      // 这样 LocalStorage 里的 nickname 就不再是空的了
      localStorage.setItem("user", JSON.stringify(res.data.data));

      // 更新成功后，稍微延迟一下跳回首页，或者让用户手动点返回
      console.log("最新缓存已存入:", res.data.data);
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (error) {
    ElMessage.error("保存失败，请检查网络");
  }
};
</script>

<style scoped>
.profile-page {
  padding: 40px;
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  justify-content: center;
}
.box-card {
  width: 100%;
  max-width: 600px;
  height: fit-content;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
