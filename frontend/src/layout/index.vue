<template>
  <div class="shell">
    <!-- 左侧导航：与页面同底色，无分割线，构成 L 形框架 -->
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand">
        <svg class="mark" width="28" height="28" viewBox="0 0 28 28" aria-hidden="true">
          <rect x="1" y="1" width="26" height="26" rx="6" fill="none" stroke="#131416" stroke-width="1.5" />
          <rect x="7" y="7" width="6" height="6" rx="1.5" fill="#1b6c80" />
          <rect x="15" y="7" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
          <rect x="7" y="15" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
          <rect x="15" y="15" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
        </svg>
        <div class="brand-text">
          <span class="brand-name">实验室管理</span>
          <span class="brand-scope">实验室中心 · 预约与设备</span>
        </div>
        <button class="close-btn" aria-label="关闭菜单" @click="mobileOpen = false">
          <AppIcon name="close" :size="18" />
        </button>
      </div>

      <nav class="nav" aria-label="主导航">
        <button
          v-for="item in menus"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
          :aria-current="route.path === item.path ? 'page' : undefined"
          @click="go(item.path)"
        >
          <AppIcon :name="item.icon" :size="16" />
          <span class="nav-label">{{ item.name }}</span>
          <span v-if="item.count" class="nav-count">{{ item.count }}</span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <div class="user-row">
          <span class="avatar">{{ initial }}</span>
          <div class="user-text">
            <span class="user-name">{{ userStore.displayName }}</span>
            <span class="user-role">{{ roleText }}</span>
          </div>
          <button class="icon-btn" title="退出登录" aria-label="退出登录" @click="handleLogout">
            <AppIcon name="logout" :size="16" />
          </button>
        </div>
      </div>
    </aside>

    <div class="scrim" v-if="mobileOpen" @click="mobileOpen = false"></div>

    <!-- 右侧：顶栏 + 白色工作台 -->
    <div class="frame">
      <header class="topbar">
        <button class="menu-btn" aria-label="打开菜单" @click="mobileOpen = true">
          <AppIcon name="menu" :size="18" />
        </button>
        <span class="topbar-scope">实验室中心</span>
        <span class="topbar-user">{{ userStore.displayName }} · {{ roleText }}</span>
      </header>

      <main class="sheet">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import AppIcon from '@/components/AppIcon.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const mobileOpen = ref(false)

const allMenus = [
  { path: '/dashboard', name: '数据看板', icon: 'overview' },
  { path: '/labs', name: '实验室管理', icon: 'lab' },
  { path: '/devices', name: '设备管理', icon: 'device' },
  { path: '/reservations', name: '我的预约', icon: 'calendar' },
  { path: '/audit', name: '预约审核', icon: 'audit', roles: ['admin', 'teacher'] },
  { path: '/files', name: '文件上传', icon: 'file' },
  { path: '/logs', name: '操作日志', icon: 'log', roles: ['admin'] }
]

const menus = computed(() => {
  const role = userStore.role
  return allMenus.filter((item) => !item.roles || item.roles.includes(role))
})

const roleText = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userStore.role] || '访客'
})

const initial = computed(() => {
  const name = userStore.displayName || '访'
  return name.slice(0, 1)
})

watch(
  () => route.path,
  () => {
    mobileOpen.value = false
  }
)

function go(path) {
  router.push(path)
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('退出后需要重新登录，确认退出？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped>
.shell {
  display: flex;
  height: 100vh;
  background: var(--ground);
  overflow: hidden;
}

/* ---------- 侧栏：与页面同底，无右分割线 ---------- */
.sidebar {
  width: var(--sidebar-width);
  flex: none;
  display: flex;
  flex-direction: column;
  padding: 16px 12px 12px;
  background: var(--ground);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 20px;
}

.brand-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: -0.01em;
}

.brand-scope {
  margin-top: 2px;
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.close-btn {
  display: none;
  margin-left: auto;
  padding: 6px;
  border: none;
  background: none;
  color: var(--muted);
  cursor: pointer;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 34px;
  padding: 0 10px;
  border: none;
  border-radius: 6px;
  background: none;
  color: var(--muted);
  font-size: var(--fs-body);
  text-align: left;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--raised);
  color: var(--text);
}

.nav-item.active {
  background: var(--active);
  color: var(--text);
  font-weight: 500;
}

.nav-item.active :deep(.app-icon) {
  color: var(--accent);
}

.nav-label {
  flex: 1;
  white-space: nowrap;
}

.nav-count {
  min-width: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--nav-count-bg, #c4242a);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}

.sidebar-foot {
  margin-top: auto;
  padding-top: 16px;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
}

.user-row:hover {
  background: var(--raised);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex: none;
  border-radius: 50%;
  background: var(--accent-tint);
  color: #145364;
  font-size: 12px;
  font-weight: 500;
}

.user-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 11px;
  color: var(--muted);
}

.icon-btn {
  margin-left: auto;
  padding: 6px;
  border: none;
  border-radius: 4px;
  background: none;
  color: var(--muted);
  cursor: pointer;
}

.icon-btn:hover {
  background: var(--active);
  color: var(--text);
}

/* ---------- 右侧框架 ---------- */
.frame {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: var(--topbar-height);
  padding: 0 24px 0 20px;
  background: var(--ground);
}

.menu-btn {
  display: none;
  padding: 6px;
  border: none;
  background: none;
  color: var(--text);
  cursor: pointer;
}

.topbar-scope {
  font-size: var(--fs-body);
  color: var(--muted);
}

.topbar-user {
  margin-left: auto;
  font-size: var(--fs-small);
  color: var(--muted);
}

/* 白色工作台：只比框架低一个色阶，无阴影 */
.sheet {
  flex: 1;
  margin: 0 var(--sheet-inset) var(--sheet-inset) 0;
  padding: var(--pad);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-sheet);
  overflow-y: auto;
  min-height: 0;
}

.scrim {
  display: none;
}

@media (max-width: 900px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 30;
    width: 260px;
    background: var(--surface);
    border-right: 1px solid var(--border);
    transform: translateX(-100%);
    transition: transform 0.2s ease;
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .close-btn {
    display: block;
  }

  .scrim {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 20;
    background: rgba(19, 20, 22, 0.28);
  }

  .frame {
    width: 100%;
  }

  .topbar {
    padding: 0 16px;
  }

  .menu-btn {
    display: block;
  }

  .topbar-user {
    display: none;
  }

  .sheet {
    margin: 0;
    border: none;
    border-radius: 0;
    padding: 16px;
  }
}
</style>
