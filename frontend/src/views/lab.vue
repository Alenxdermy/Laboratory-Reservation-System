<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>实验室管理</h1>
        <p class="title-sub">
          在册 {{ labs.length }} 间，可用 {{ availableCount }} 间，维护中 {{ labs.length - availableCount }} 间
        </p>
      </div>
      <div class="title-actions" v-if="userStore.isManager">
        <el-button type="primary" @click="openCreate">新增实验室</el-button>
      </div>
    </div>

    <div class="panel">
      <table class="data-table">
        <thead>
          <tr>
            <th>实验室</th>
            <th class="num">容量</th>
            <th>状态</th>
            <th class="time-col">创建时间</th>
            <th v-if="userStore.isManager" class="action-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in labs" :key="row.id">
            <td>
              <span>{{ row.name }}</span>
              <span class="secondary">{{ row.description || '—' }}</span>
            </td>
            <td class="num">{{ row.capacity }}<span class="unit">人</span></td>
            <td>
              <span class="pill" :class="row.status === 'available' ? 'pill-positive' : 'pill-warning'">
                {{ row.status === 'available' ? '空闲' : '维护中' }}
              </span>
            </td>
            <td class="time-col mono">{{ row.createTime || '—' }}</td>
            <td v-if="userStore.isManager" class="action-col">
              <button class="text-action" @click="openEdit(row)">编辑</button>
              <button class="text-action danger" @click="handleDelete(row)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="table-empty" v-if="loading">加载中…</p>
      <p class="table-empty" v-else-if="labs.length === 0">还没有实验室，先新增一间。</p>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑实验室' : '新增实验室'"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="实验室名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：计算机实验室A" />
        </el-form-item>
        <el-form-item label="容量（人）" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="available">空闲</el-radio>
            <el-radio value="maintenance">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="设备配置、位置等（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import labApi from '@/api/lab'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const labs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const availableCount = computed(() => labs.value.filter((l) => l.status === 'available').length)

const form = reactive({
  id: null,
  name: '',
  capacity: 30,
  status: 'available',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入实验室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function loadLabs() {
  loading.value = true
  try {
    const res = await labApi.list()
    labs.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.name = ''
  form.capacity = 30
  form.status = 'available'
  form.description = ''
  formRef.value && formRef.value.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.id = row.id
  form.name = row.name
  form.capacity = row.capacity
  form.status = row.status
  form.description = row.description
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: form.name,
      capacity: form.capacity,
      status: form.status,
      description: form.description
    }
    if (form.id) {
      await labApi.update(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await labApi.create(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadLabs()
  } catch (e) {
    // 错误提示由拦截器处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`删除后不可恢复，确认删除「${row.name}」？`, '删除实验室', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await labApi.remove(row.id)
    ElMessage.success('删除成功')
    await loadLabs()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadLabs)
</script>

<style scoped>
.unit {
  margin-left: 4px;
  font-size: var(--fs-small);
  color: var(--muted);
}

.time-col {
  width: 170px;
  white-space: nowrap;
}

.action-col {
  width: 110px;
  text-align: right;
  white-space: nowrap;
}

@media (max-width: 700px) {
  .time-col {
    display: none;
  }
}
</style>
