<template>
  <div class="auth-page">
    <!-- 左侧：产品自身的时段表，作为登录页的构图主体 -->
    <section class="auth-context">
      <span class="context-label">实验室中心</span>
      <p class="context-statement">实验时段、设备状态与审批，在一个工作台里完成。</p>
      <div class="week-grid" aria-hidden="true">
        <div class="week-head">
          <span v-for="day in days" :key="day">{{ day }}</span>
        </div>
        <div class="week-body">
          <div class="week-times">
            <span v-for="t in times" :key="t">{{ t }}</span>
          </div>
          <div class="week-cells">
            <span
              v-for="(cell, index) in cells"
              :key="index"
              class="cell"
              :class="cell"
            ></span>
          </div>
        </div>
      </div>
      <p class="context-caption">一周可用时段示意 · 审核通过后占用</p>
    </section>

    <div class="auth-sheet">
      <div class="auth-brand">
        <svg width="32" height="32" viewBox="0 0 28 28" aria-hidden="true">
          <rect x="1" y="1" width="26" height="26" rx="6" fill="none" stroke="#131416" stroke-width="1.5" />
          <rect x="7" y="7" width="6" height="6" rx="1.5" fill="#1b6c80" />
          <rect x="15" y="7" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
          <rect x="7" y="15" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
          <rect x="15" y="15" width="6" height="6" rx="1.5" fill="none" stroke="#5d5f60" stroke-width="1.5" />
        </svg>
        <div>
          <h1>实验室管理</h1>
          <p>预约、设备与使用率，一处管理</p>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model.trim="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          class="submit-btn"
          type="primary"
          :loading="loading"
          size="large"
          @click="handleLogin"
        >
          {{ loading ? '登录中…' : '登录' }}
        </el-button>
      </el-form>

      <p class="auth-switch">
        还没有账号？<router-link to="/register">注册新账号</router-link>
      </p>

      <div class="demo-well">
        <span class="demo-title">演示账号</span>
        <span>管理员 admin / 123456</span>
        <span>教师 teacher01 / 123456</span>
        <span>学生 student01 / 123456</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const days = ['一', '二', '三', '四', '五', '六', '日']
const times = ['08:00', '10:00', '14:00', '16:00', '19:00']
// 仅作构图示意：空、浅色占用、强调占用
const cells = [
  'on', '', 'on', '', 'on', '', '',
  '', 'on', '', 'on', '', '', '',
  'on', 'on', '', 'on', '', '', '',
  '', '', 'on', '', 'on', '', '',
  'focus', '', '', 'on', '', '', 'on'
]

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } catch (e) {
    // 错误提示由响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 72px;
  min-height: 100vh;
  padding: 48px;
  background: var(--ground);
}

.auth-context {
  width: 420px;
  flex: none;
}

.context-label {
  font-size: var(--fs-small);
  color: var(--muted);
}

.context-statement {
  margin: 12px 0 28px;
  max-width: 18em;
  font-size: 28px;
  line-height: 1.35;
  font-weight: 500;
  letter-spacing: -0.02em;
  color: var(--text);
}

.week-grid {
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  background: var(--surface);
  overflow: hidden;
}

.week-head {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  padding: 8px 8px 4px 44px;
  font-size: var(--fs-small);
  color: var(--muted);
  text-align: center;
}

.week-body {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  padding: 0 8px 8px;
}

.week-times {
  display: grid;
  grid-template-rows: repeat(5, 26px);
  align-items: center;
  font-size: 11px;
  color: var(--muted);
}

.week-cells {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(5, 26px);
  gap: 3px;
}

.cell {
  grid-row: auto;
  border-radius: 3px;
  background: var(--raised);
}

.cell.on {
  background: var(--accent-tint);
}

.cell.focus {
  background: var(--accent);
}

.context-caption {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.auth-sheet {
  width: 380px;
  max-width: 100%;
  flex: none;
  padding: 32px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-sheet);
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.auth-brand h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text);
}

.auth-brand p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.submit-btn {
  width: 100%;
  height: 40px;
  margin-top: 4px;
}

.auth-switch {
  margin: 20px 0 0;
  font-size: 13px;
  color: var(--muted);
  text-align: center;
}

.demo-well {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 20px;
  padding: 12px 14px;
  border-radius: var(--radius-card);
  background: var(--raised);
  font-size: 13px;
  color: var(--muted);
}

.demo-title {
  color: var(--text);
  font-weight: 500;
}

@media (max-width: 1024px) {
  .auth-page {
    gap: 0;
    padding: 24px;
  }

  .auth-context {
    display: none;
  }
}
</style>
