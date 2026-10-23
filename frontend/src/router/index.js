import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'labs',
        name: 'labs',
        component: () => import('@/views/lab.vue'),
        meta: { title: '实验室管理' }
      },
      {
        path: 'devices',
        name: 'devices',
        component: () => import('@/views/device.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'reservations',
        name: 'reservations',
        component: () => import('@/views/reservation.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'audit',
        name: 'audit',
        component: () => import('@/views/audit.vue'),
        meta: { title: '预约审核', roles: ['admin', 'teacher'] }
      },
      {
        path: 'files',
        name: 'files',
        component: () => import('@/views/upload.vue'),
        meta: { title: '文件上传' }
      },
      {
        path: 'logs',
        name: 'logs',
        component: () => import('@/views/log.vue'),
        meta: { title: '操作日志', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守卫：
 * 1. 未登录访问受保护页面 -> 跳转登录
 * 2. 已登录访问登录/注册页 -> 跳转首页
 * 3. 角色不匹配 -> 跳转数据看板
 */
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.public && token) {
    return { path: '/dashboard' }
  }
  if (to.meta.roles && user && !to.meta.roles.includes(user.role)) {
    return { path: '/dashboard' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 实验室预约与设备管理系统`
    : '实验室预约与设备管理系统'
})

export default router
