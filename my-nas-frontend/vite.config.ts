import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite' // 注意：这是 Tailwind v4 的插件写法

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
  ],
  // --- 新增 server 配置 ---
  server: {
    proxy: {
      // 意思是：只要看到 /api 开头的请求，都转发给后端
      '/api': {
        target: 'http://192.168.177.133:8080', // 后端地址
        changeOrigin: true, // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 去掉 /api 前缀
        // 解释：前端发 /api/login -> 转发给后端变成 /login
      }
    }
  }
})
