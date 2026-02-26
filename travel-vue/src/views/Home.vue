<template>
  <div class="common-layout">
    <el-container>
      <!-- 1. 头部区域 -->
      <el-header
        style="
          background-color: #409eff;
          color: white;
          display: flex;
          align-items: center;
          justify-content: space-between;
        "
      >
        <div
          style="display: flex; align-items: center; gap: 10px; cursor: pointer"
          @click="router.push('/home')"
        >
          <el-icon size="24"><Location /></el-icon>
          <h2 style="margin: 0">智慧旅游信息管理系统</h2>
        </div>

        <div style="display: flex; align-items: center; gap: 20px">
          <el-link
            :underline="false"
            style="color: white"
            @click="router.push('/favorites')"
          >
            <el-icon><Star /></el-icon> 我的收藏
          </el-link>

          <el-button
            v-if="loginUser.role === 'ADMIN'"
            type="warning"
            size="small"
            @click="router.push('/admin')"
          >
            后台管理
          </el-button>

          <el-link
            :underline="false"
            style="color: white"
            @click="router.push('/profile')"
          >
            <el-icon><User /></el-icon> 个人资料
          </el-link>

          <span>欢迎您，{{ loginUser.nickname || loginUser.username }}</span>
          <el-button size="small" type="danger" @click="handleLogout"
            >退出登录</el-button
          >
        </div>
      </el-header>

      <el-main>
        <!-- 2. 智慧推荐栏 -->
        <div class="section-title">
          <el-icon color="#f56c6c"><MagicStick /></el-icon> 猜你喜欢
          (实时算法驱动)
        </div>
        <el-row :gutter="20" class="recommend-box">
          <el-col
            :span="6"
            v-for="item in recommendList"
            :key="'rec-' + item.id"
          >
            <el-card
              :body-style="{ padding: '0px' }"
              shadow="always"
              class="rec-card"
            >
              <img :src="item.imageUrl" class="card-img" />
              <div style="padding: 10px">
                <span class="attr-name">{{ item.name }}</span>
                <div class="attr-info">{{ item.city }}</div>
                <!-- 修改：点击跳转详情页 -->
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="router.push('/detail/' + item.id)"
                  >立即查看</el-button
                >
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider />

        <!-- 3. 搜索栏 -->
        <div style="margin: 20px 0; display: flex; gap: 10px">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索名称或城市（如：温州）"
            style="width: 300px"
            clearable
            @clear="fetchData('')"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>

        <!-- 4. 全部景点展示 -->
        <div class="section-title">探索全部景点 (共 {{ total }} 个)</div>
        <el-row :gutter="20">
          <el-col
            :span="6"
            v-for="item in attractionList"
            :key="item.id"
            style="margin-bottom: 20px"
          >
            <el-card :body-style="{ padding: '0px' }" shadow="hover">
              <img :src="item.imageUrl" class="card-img" />
              <div style="padding: 14px">
                <span style="font-weight: bold; font-size: 16px">{{
                  item.name
                }}</span>
                <div class="attr-info">
                  {{ item.city }} · {{ item.address }}
                </div>
                <div class="card-footer">
                  <span class="price">¥{{ item.price }}</span>
                  <!-- 修改：点击跳转详情页 -->
                  <el-button
                    type="primary"
                    size="small"
                    @click="router.push('/detail/' + item.id)"
                    >查看详情</el-button
                  >
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { MagicStick, Location, Star, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();

// 用户逻辑
const userStr = localStorage.getItem("user");
const loginUser = userStr ? JSON.parse(userStr) : {};
const currentUserId = loginUser.id;

if (!currentUserId) {
  router.push("/login");
}

// 数据
const attractionList = ref([]);
const recommendList = ref([]);
const total = ref(0);
const searchKeyword = ref("");

// 逻辑函数
const fetchData = async (keyword = "") => {
  try {
    const res = await axios.get("http://localhost:8080/attraction/page", {
      params: { current: 1, size: 20, name: keyword },
    });
    if (res.data.code === 200) {
      attractionList.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch (error) {
    console.error("加载数据失败:", error);
  }
};

const handleSearch = () => fetchData(searchKeyword.value);
const resetSearch = () => {
  searchKeyword.value = "";
  fetchData();
};

const fetchRecommendations = async () => {
  try {
    const res = await axios.get(
      `http://localhost:8080/attraction/recommend?userId=${currentUserId}&type=item`,
    );
    if (res.data.code === 200) recommendList.value = res.data.data;
  } catch (e) {
    console.error("获取推荐失败:", e);
  }
};

const handleLogout = () => {
  localStorage.removeItem("user");
  router.push("/login");
};

onMounted(() => {
  if (currentUserId) {
    fetchData();
    fetchRecommendations();
  }
});
</script>

<style scoped>
.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.recommend-box {
  background-color: #f0f9ff;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  border: 1px dashed #409eff;
}
.card-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.attr-name {
  font-weight: bold;
  display: block;
  margin-bottom: 5px;
  color: #333;
}
.attr-info {
  color: #999;
  font-size: 12px;
  margin-bottom: 10px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}
</style>
