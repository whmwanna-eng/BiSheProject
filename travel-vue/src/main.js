import { createApp } from "vue";
import App from "./App.vue";
import router from "./router"; // 确保路径正确

import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";

const app = createApp(App);

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(ElementPlus);
app.use(router); // 必须在 mount 之前
app.mount("#app");
