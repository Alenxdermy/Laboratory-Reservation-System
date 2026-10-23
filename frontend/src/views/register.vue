<template>
  <div class="auth-page">
    <div class="auth-sheet">
      <div class="auth-head">
        <h1>注册账号</h1>
        <p>学生与教师账号可自助注册，管理员由系统分配</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="3-50 位，登录时使用" size="large" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model.trim="form.realName" placeholder="用于预约记录展示" size="large" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" size="large" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model.trim="form.password"
            type="password"
            show-password
            placeholder="至少 6 位"
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model.trim="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入密码"
            size="large"
          />
        </el-form-item>

        <el-button
          class="submit-btn"
          type="primary"
          :loading="loading"
          size="large"
          @click="handleRegister"
        >
          {{ loading ? '提交中…' : '注册' }}
        </el-button>
      </el-form>

      <p class="auth-switch">
        已有账号？<router-link to="/login">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  role: 'student',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      realName: form.realName,
      role: form.role,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误提示由拦截器处理
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
  min-height: 100vh;
  padding: 24px;
  background: var(--ground);
}

.auth-sheet {
  width: 380px;
  max-width: 100%;
  padding: 32px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-sheet);
}

.auth-head {
  margin-bottom: 24px;
}

.auth-head h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
}

.auth-head p {
  margin: 6px 0 0;
  font-size: var(--fs-small);
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
</style>
