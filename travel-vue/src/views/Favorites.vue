<template>
  <div class="common-layout">
    <el-container>
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
          <el-icon size="24"><Back /></el-icon>
          <h2>我的收藏夹</h2>
        </div>
        <el-button @click="router.push('/home')">返回首页</el-button>
      </el-header>

      <el-main>
        <el-empty
          v-if="favList.length === 0"
          description="您还没有收藏任何景点哦，快去首页看看吧！"
        >
          <el-button type="primary" @click="router.push('/home')"
            >去探索</el-button
          >
        </el-empty>

        <el-row :gutter="20">
          <el-col
            :span="6"
            v-for="item in favList"
            :key="item.id"
            style="margin-bottom: 20px"
          >
            <el-card :body-style="{ padding: '0px' }" shadow="hover">
              <img
                :src="item.imageUrl"
                style="width: 100%; height: 200px; object-fit: cover"
              />
              <div style="padding: 14px">
                <span style="font-weight: bold; font-size: 16px">{{
                  item.name
                }}</span>
                <div style="margin-top: 10px; color: #999; font-size: 12px">
                  {{ item.city }}
                </div>
                <div
                  style="
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-top: 15px;
                  "
                >
                  <span
                    style="color: #f56c6c; font-size: 18px; font-weight: bold"
                    >¥{{ item.price }}</span
                  >
                  <div style="display: flex; gap: 5px">
                    <!-- 新增：收藏夹跳转详情按钮 -->
                    <el-button
                      size="small"
                      @click="router.push('/detail/' + item.id)"
                      >详情</el-button
                    >
                    <el-button
                      type="danger"
                      plain
                      size="small"
                      @click="removeFavorite(item.id)"
                      >取消</el-button
                    >
                  </div>
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
import { useRouter } from "vue-router";
import axios from "axios";
import { ElMessage } from "element-plus";
import { Back } from "@element-plus/icons-vue";

const router = useRouter();
const favList = ref([]);
const user = JSON.parse(localStorage.getItem("user") || "{}");

const fetchFavData = async () => {
  if (!user.id) return;
  try {
    const res = await axios.get(
      "http://localhost:8080/attraction/my-favorites",
      {
        params: { userId: user.id },
      },
    );
    if (res.data.code === 200) favList.value = res.data.data;
  } catch (error) {
    console.error("加载收藏夹失败:", error);
  }
};

const removeFavorite = async (attrId) => {
  try {
    await axios.post("http://localhost:8080/favorite/toggle", {
      userId: user.id,
      attractionId: attrId,
    });
    ElMessage.success("已移除出收藏夹");
    fetchFavData();
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

onMounted(fetchFavData);
</script>
