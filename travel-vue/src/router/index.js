import { createRouter, createWebHistory } from "vue-router";
import Login from "../views/Login.vue";
import Home from "../views/Home.vue";
import AdminLogin from "../views/AdminLogin.vue";
import AdminLayout from "../views/AdminLayout.vue";
import AdminAttraction from "../views/Admin.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: Login },
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/Register.vue"),
  },
  { path: "/home", name: "Home", component: Home },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("../views/Profile.vue"),
  },
  { path: "/admin/login", name: "AdminLogin", component: AdminLogin },
  {
    path: "/admin",
    component: AdminLayout,
    redirect: "/admin/attraction",
    children: [
      {
        path: "attraction",
        name: "AdminAttraction",
        component: AdminAttraction,
      },
      {
        path: "category",
        name: "AdminCategory",
        component: () => import("../views/AdminCategory.vue"),
      },
      {
        path: "user",
        name: "AdminUser",
        component: () => import("../views/AdminUser.vue"),
      },
    ],
  },
  {
    path: "/favorites",
    name: "Favorites",
    component: () => import("../views/Favorites.vue"),
  },
  {
    path: "/detail/:id",
    name: "Detail",
    component: () => import("../views/Detail.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/**
 * 全局路由守卫 - 修复死循环与解析崩溃
 */
router.beforeEach((to, from, next) => {
  // 1. 安全读取用户信息
  let user = null;
  try {
    const userStr = localStorage.getItem("user");
    if (userStr && userStr !== "undefined" && userStr !== "null") {
      user = JSON.parse(userStr);
    }
  } catch (e) {
    localStorage.removeItem("user");
  }

  // 2. 定义白名单
  const whiteList = ["/login", "/register", "/admin/login"];

  // 3. 如果在白名单中，直接放行，不走后续逻辑
  if (whiteList.includes(to.path)) {
    next();
    return;
  }

  // 4. 未登录拦截
  if (!user) {
    if (to.path.startsWith("/admin")) {
      next("/admin/login");
    } else {
      next("/login");
    }
    return;
  }

  // 5. 管理员权限检查
  if (to.path.startsWith("/admin") && user.role !== "ADMIN") {
    next("/home");
    return;
  }

  // 6. 最终放行
  next();
});

export default router;
