import { defineStore } from 'pinia'
import authApi from '@/api/auth'

/**
 * 用户状态管理：Token 与登录用户信息持久化到 localStorage
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    isManager: (state) => ['admin', 'teacher'].includes(state.user?.role),
    isAdmin: (state) => state.user?.role === 'admin',
    displayName: (state) => state.user?.realName || state.user?.username || '未登录'
  },
  actions: {
    async login(form) {
      const res = await authApi.login(form)
      this.token = res.data.token
      this.user = res.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return res.data.user
    },
    async fetchCurrentUser() {
      const res = await authApi.getCurrentUser()
      this.user = res.data
      localStorage.setItem('user', JSON.stringify(this.user))
      return res.data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
