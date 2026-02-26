<template>
  <div style="padding: 20px">
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
      "
    >
      <div style="display: flex; align-items: center; gap: 10px">
        <el-button icon="Back" circle @click="router.push('/home')" />
        <h2 style="margin: 0">景点数据管理后台</h2>
      </div>
      <el-button type="success" icon="Plus" @click="handleAdd"
        >新增景点数据</el-button
      >
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="景点名称" width="150" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="price" label="门票(元)" width="100" />
      <el-table-column label="预览图" width="120">
        <template #default="scope">
          <el-image
            :src="scope.row.imageUrl"
            style="width: 80px; height: 50px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="address" label="详细地址" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row.id)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑景点' : '新增景点'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="景点名称"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="城市"
          ><el-input v-model="form.city"
        /></el-form-item>
        <el-form-item label="分类ID"
          ><el-input-number v-model="form.categoryId" :min="1"
        /></el-form-item>
        <el-form-item label="详细地址"
          ><el-input v-model="form.address"
        /></el-form-item>
        <el-form-item label="价格"
          ><el-input-number v-model="form.price" :min="0"
        /></el-form-item>
        <el-form-item label="经度"
          ><el-input v-model="form.longitude"
        /></el-form-item>
        <el-form-item label="纬度"
          ><el-input v-model="form.latitude"
        /></el-form-item>
        <el-form-item label="图片URL"
          ><el-input v-model="form.imageUrl"
        /></el-form-item>
        <el-form-item label="景点描述"
          ><el-input v-model="form.description" type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const tableData = ref([]);
const dialogVisible = ref(false);
const form = ref({});

// 加载所有数据
const loadData = async () => {
  const res = await axios.get("http://localhost:8080/attraction/page?size=100");
  if (res.data.code === 200) tableData.value = res.data.data.records;
};

// 打开新增弹窗
const handleAdd = () => {
  form.value = { price: 0, categoryId: 1 };
  dialogVisible.value = true;
};

// 打开编辑弹窗
const handleEdit = (row) => {
  form.value = { ...row };
  dialogVisible.value = true;
};

// 保存逻辑（新增或修改）
const saveForm = async () => {
  const method = form.value.id ? "put" : "post";
  const url = form.value.id
    ? "http://localhost:8080/attraction/update"
    : "http://localhost:8080/attraction/save";

  const res = await axios[method](url, form.value);
  if (res.data.code === 200) {
    ElMessage.success("操作成功");
    dialogVisible.value = false;
    loadData();
  }
};

// 删除逻辑
const handleDelete = (id) => {
  ElMessageBox.confirm(
    "确定要删除该景点吗？这将影响推荐算法的数据集！",
    "警告",
    { type: "warning" },
  ).then(async () => {
    const res = await axios.delete(
      `http://localhost:8080/attraction/delete/${id}`,
    );
    if (res.data.code === 200) {
      ElMessage.success("删除成功");
      loadData();
    }
  });
};

onMounted(loadData);
</script>
