<template>
  <el-card>
    <template #header>
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <span>分类标签管理</span>
        <el-button type="primary" @click="handleAdd">添加分类</el-button>
      </div>
    </template>

    <el-table :data="categoryList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" width="180" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(scope.row.id)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加分类弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增分类" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";

const categoryList = ref([]);
const dialogVisible = ref(false);
const form = ref({ name: "", description: "" });

// 获取列表
const loadCategories = async () => {
  const res = await axios.get("http://localhost:8080/category/list");
  if (res.data.code === 200) categoryList.value = res.data.data;
};

const handleAdd = () => {
  form.value = { name: "", description: "" };
  dialogVisible.value = true;
};

// 保存
const saveCategory = async () => {
  const res = await axios.post(
    "http://localhost:8080/category/add",
    form.value,
  );
  if (res.data.code === 200) {
    ElMessage.success("添加成功");
    dialogVisible.value = false;
    loadCategories();
  }
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm("确定删除该分类吗？", "提示", { type: "warning" }).then(
    async () => {
      const res = await axios.delete(
        `http://localhost:8080/category/delete/${id}`,
      );
      if (res.data.code === 200) {
        ElMessage.success("删除成功");
        loadCategories();
      }
    },
  );
};

onMounted(loadCategories);
</script>
