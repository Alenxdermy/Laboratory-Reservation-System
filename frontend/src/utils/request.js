import axios from 'axios'
import { ElMessage } from 'element-plus'

// 开发环境通过 vite 代理转发到后端，生产环境可用 VITE_API_BASE 覆盖
const BASE_URL = import.meta.env.VITE_API_BASE || '/lab-system/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 Result { code, msg, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401) {
        // Token 失效：清除本地登录态并回到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
