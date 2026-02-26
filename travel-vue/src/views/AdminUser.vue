<template>
  <el-card>
    <template #header>用户权限与角色管理</template>
    <el-table :data="userList" border stripe>
      <el-table-column prop="id" label="UID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="当前角色">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ scope.row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <!-- 角色切换按钮 -->
          <el-button
            size="small"
            :type="scope.row.role === 'ADMIN' ? 'info' : 'warning'"
            @click="changeRole(scope.row)"
          >
            {{ scope.row.role === "ADMIN" ? "降级为普通用户" : "设为管理员" }}
          </el-button>

          <!-- 封禁按钮 -->
          <el-button
            size="small"
            type="danger"
            @click="handleDeleteUser(scope.row)"
            >封禁</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
// 记得引入 ElMessage 和 ElMessageBox
import { ElMessage, ElMessageBox } from "element-plus";

const userList = ref([]);

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await axios.get("http://localhost:8080/user/list");
    if (res.data.code === 200) {
      userList.value = res.data.data;
    }
  } catch (error) {
    console.error("加载用户失败:", error);
  }
};

// 修改用户角色逻辑
const changeRole = (row) => {
  // --- 核心安全检查：禁止修改自己的权限 ---
  const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
  if (row.id === currentUser.id) {
    ElMessage.warning("为了防止失去管理权限，系统禁止管理员修改自己的角色。");
    return;
  }

  const newRole = row.role === "ADMIN" ? "USER" : "ADMIN";
  const actionText = newRole === "ADMIN" ? "设为管理员" : "降级为普通用户";

  ElMessageBox.confirm(
    `确定要将用户 [${row.username}] ${actionText} 吗？`,
    "权限变更确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(async () => {
    const res = await axios.put("http://localhost:8080/user/role", {
      id: row.id,
      role: newRole,
    });
    if (res.data.code === 200) {
      ElMessage.success("权限变更成功");
      loadUsers(); // 刷新列表
    }
  });
};

// 补全缺失的删除（封禁）逻辑
const handleDeleteUser = (row) => {
  // 防止管理员误删自己
  const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
  if (row.id === currentUser.id) {
    ElMessage.error("不能封禁你自己！");
    return;
  }

  ElMessageBox.confirm(
    `确定要封禁并删除用户 [${row.username}] 吗？此操作不可逆！`,
    "严正警告",
    {
      confirmButtonText: "确定封禁",
      cancelButtonText: "取消",
      type: "error",
    },
  ).then(async () => {
    // 对应你后端 UserController 的 @DeleteMapping("/{id}")
    const res = await axios.delete(`http://localhost:8080/user/${row.id}`);
    if (res.data.code === 200) {
      ElMessage.success("该用户已被永久封禁");
      loadUsers(); // 刷新列表
    }
  });
};

onMounted(loadUsers);
</script>
