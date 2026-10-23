import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 覆盖父目录（原 Vue2 工程）的 postcss.config.js，避免加载未安装的 autoprefixer
  css: {
    postcss: {
      plugins: []
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 代理后端接口与上传文件，避免开发环境跨域
      '/lab-system': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
