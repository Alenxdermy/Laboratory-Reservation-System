<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>设备管理</h1>
        <p class="title-sub">
          在册 {{ devices.length }} 台，空闲 {{ availableCount }} 台，维修中 {{ maintenanceCount }} 台
        </p>
      </div>
      <div class="title-actions">
        <el-select
          v-model="query.labId"
          placeholder="全部实验室"
          clearable
          style="width: 160px"
          @change="loadDevices"
        >
          <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
        </el-select>
        <el-input
          v-model.trim="query.keyword"
          placeholder="搜索设备名称或型号"
          clearable
          style="width: 200px"
          @keyup.enter="loadDevices"
          @clear="loadDevices"
        />
        <el-button v-if="userStore.isManager" type="primary" @click="openCreate">新增设备</el-button>
      </div>
    </div>

    <div class="panel">
      <table class="data-table">
        <thead>
          <tr>
            <th>设备</th>
            <th>所属实验室</th>
            <th>状态</th>
            <th class="time-col">入库时间</th>
            <th v-if="userStore.isManager" class="action-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in devices" :key="row.id">
            <td>
              <span>{{ row.name }}</span>
              <span class="secondary mono">{{ row.model || '型号未登记' }}</span>
            </td>
            <td>{{ row.labName || '—' }}</td>
            <td>
              <span class="pill" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
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
      <p class="table-empty" v-else-if="devices.length === 0">当前筛选条件下没有设备。</p>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑设备' : '新增设备'"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="所属实验室" prop="labId">
          <el-select v-model="form.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：图形工作站" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model.trim="form.model" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="空闲" value="available" />
            <el-option label="占用" value="occupied" />
            <el-option label="维修" value="maintenance" />
          </el-select>
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
import deviceApi from '@/api/device'
import labApi from '@/api/lab'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const labs = ref([])
const devices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const query = reactive({ labId: null, keyword: '' })
const form = reactive({ id: null, labId: null, name: '', model: '', status: 'available' })

const availableCount = computed(() => devices.value.filter((d) => d.status === 'available').length)
const maintenanceCount = computed(() => devices.value.filter((d) => d.status === 'maintenance').length)

const rules = {
  labId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function statusText(status) {
  return { available: '空闲', occupied: '占用', maintenance: '维修' }[status] || status
}

function statusClass(status) {
  return {
    available: 'pill-positive',
    occupied: 'pill-neutral',
    maintenance: 'pill-warning'
  }[status] || 'pill-neutral'
}

async function loadLabs() {
  const res = await labApi.list()
  labs.value = res.data || []
}

async function loadDevices() {
  loading.value = true
  try {
    const res = await deviceApi.list({
      labId: query.labId || undefined,
      keyword: query.keyword || undefined
    })
    devices.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.labId = null
  form.name = ''
  form.model = ''
  form.status = 'available'
  formRef.value && formRef.value.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.id = row.id
  form.labId = row.labId
  form.name = row.name
  form.model = row.model
  form.status = row.status
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
      labId: form.labId,
      name: form.name,
      model: form.model,
      status: form.status
    }
    if (form.id) {
      await deviceApi.update(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await deviceApi.create(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadDevices()
  } catch (e) {
    // 错误提示由拦截器处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`删除后不可恢复，确认删除「${row.name}」？`, '删除设备', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deviceApi.remove(row.id)
    ElMessage.success('删除成功')
    await loadDevices()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(async () => {
  await loadLabs()
  await loadDevices()
})
</script>

<style scoped>
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
