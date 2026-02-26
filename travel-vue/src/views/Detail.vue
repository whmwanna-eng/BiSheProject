<template>
  <div class="detail-container">
    <!-- 1. 顶部导航栏 -->
    <el-page-header @back="router.push('/home')" style="margin-bottom: 20px">
      <template #content>
        <span style="font-weight: bold">景点详情</span>
      </template>
    </el-page-header>

    <el-row :gutter="40">
      <!-- 左侧：图片展示 -->
      <el-col :span="12">
        <el-image
          :src="item.imageUrl"
          style="
            width: 100%;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          "
        />
      </el-col>

      <!-- 右侧：核心信息与交互 -->
      <el-col :span="12">
        <div class="info-side">
          <h1 class="title">{{ item.name }}</h1>
          <div class="tag-row">
            <el-tag effect="dark">{{ item.city }}</el-tag>
            <el-tag type="success" style="margin-left: 10px">{{
              item.openTime
            }}</el-tag>
          </div>

          <div class="price-row">
            <span class="label">门票价格：</span>
            <span class="value">¥{{ item.price }}</span>
          </div>

          <p class="description">{{ item.description }}</p>
          <div style="color: #999; font-size: 13px; margin-bottom: 30px">
            地址：{{ item.address }}
          </div>

          <!-- 交互区 -->
          <div class="action-card">
            <h3>您的互动</h3>
            <div
              style="
                display: flex;
                align-items: center;
                gap: 20px;
                margin-bottom: 15px;
              "
            >
              <span>评分：</span>
              <el-rate v-model="userScore" allow-half @change="submitRating" />
            </div>
            <el-button
              :type="isFavorited ? 'warning' : 'info'"
              :icon="Star"
              @click="toggleFavorite"
              round
            >
              {{ isFavorited ? "已收藏此景点" : "加入收藏夹" }}
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-divider style="margin: 40px 0" />

    <!-- 底部：评论区 -->
    <div class="comment-area">
      <h3>游客评论 ({{ commentList.length }})</h3>

      <div class="comment-input">
        <el-input
          v-model="commentInput"
          type="textarea"
          :rows="3"
          placeholder="写下您的旅行感悟..."
        />
        <el-button
          type="primary"
          style="margin-top: 10px"
          @click="submitComment"
          >发表评论</el-button
        >
      </div>

      <div v-for="c in commentList" :key="c.id" class="comment-item">
        <div class="comment-header">
          <span class="username">{{ c.username }}</span>
          <span class="time">{{ c.createTime }}</span>
        </div>
        <p class="content">{{ c.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { Star } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const attrId = route.params.id; // 从地址栏获取 ID

// 数据定义
const item = ref({});
const user = JSON.parse(localStorage.getItem("user") || "{}");
const userScore = ref(0);
const isFavorited = ref(false);
const commentList = ref([]);
const commentInput = ref("");

// 加载景点详情
const fetchDetail = async () => {
  const res = await axios.get(`http://localhost:8080/attraction/${attrId}`);
  if (res.data.code === 200) item.value = res.data.data;
};

// 加载交互状态
const fetchStatus = async () => {
  const res = await axios.get(
    "http://localhost:8080/attraction/interaction/status",
    {
      params: { userId: user.id, attractionId: attrId },
    },
  );
  if (res.data.code === 200) {
    userScore.value = res.data.data.score;
    isFavorited.value = res.data.data.isFavorited;
  }
};

// 加载评论
const fetchComments = async () => {
  const res = await axios.get("http://localhost:8080/comment/list", {
    params: { attractionId: attrId },
  });
  if (res.data.code === 200) commentList.value = res.data.data;
};

const submitRating = async (val) => {
  await axios.post("http://localhost:8080/rating/submit", {
    userId: user.id,
    attractionId: attrId,
    score: val,
  });
  ElMessage.success("评分已同步");
};

const toggleFavorite = async () => {
  const res = await axios.post("http://localhost:8080/favorite/toggle", {
    userId: user.id,
    attractionId: attrId,
  });
  isFavorited.value = !isFavorited.value;
  ElMessage.success(res.data.data);
};

const submitComment = async () => {
  if (!commentInput.value.trim()) return ElMessage.warning("内容不能为空");
  const res = await axios.post("http://localhost:8080/comment/add", {
    userId: user.id,
    attractionId: attrId,
    content: commentInput.value,
  });
  if (res.data.code === 200) {
    ElMessage.success("发表成功");
    commentInput.value = "";
    fetchComments();
  }
};

onMounted(() => {
  fetchDetail();
  fetchStatus();
  fetchComments();
});
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}
.info-side {
  text-align: left;
}
.title {
  font-size: 32px;
  margin: 0 0 20px 0;
}
.price-row {
  margin: 20px 0;
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}
.description {
  line-height: 1.8;
  color: #555;
  margin-bottom: 20px;
  font-size: 16px;
}
.action-card {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 12px;
}
.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  text-align: left;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.username {
  font-weight: bold;
  color: #409eff;
}
.time {
  color: #999;
  font-size: 12px;
}
</style>
