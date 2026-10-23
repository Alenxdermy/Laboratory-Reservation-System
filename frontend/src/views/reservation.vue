<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>预约实验室</h1>
        <p class="title-sub">提交后进入待审核，冲突与超员会在提交时直接拦截</p>
      </div>
    </div>

    <div class="res-grid">
      <section class="panel form-panel">
        <div class="panel-head">
          <h2>新预约</h2>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="实验室" prop="labId">
            <el-select v-model="form.labId" placeholder="请选择可用实验室" style="width: 100%">
              <el-option
                v-for="lab in availableLabs"
                :key="lab.id"
                :label="`${lab.name}（${lab.capacity} 人）`"
                :value="lab.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="使用时间段" prop="timeRange">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="使用人数" prop="people">
            <el-input-number v-model="form.people" :min="1" :max="999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="预约用途" prop="purpose">
            <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="实验内容、课程名称（选填）" />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
            提交预约
          </el-button>
          <el-button class="reset-btn" @click="resetForm">重置</el-button>
        </el-form>
      </section>

      <section class="panel list-panel">
        <div class="panel-head">
          <h2>我的预约</h2>
          <span class="panel-meta">共 {{ reservations.length }} 条</span>
        </div>

        <table class="data-table">
          <thead>
            <tr>
              <th>实验室</th>
              <th>时间段</th>
              <th class="num">人数</th>
              <th>状态</th>
              <th class="action-col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in reservations" :key="row.id">
              <td>
                <span>{{ row.labName }}</span>
                <span class="secondary">{{ row.purpose || '未填写用途' }}</span>
              </td>
              <td>
                <span>{{ shortDate(row.startTime) }}</span>
                <span class="secondary mono">{{ shortClock(row.startTime) }} – {{ shortClock(row.endTime) }}</span>
              </td>
              <td class="num">{{ row.people }}</td>
              <td>
                <span class="pill" :class="pillClass(row.status)">{{ statusText(row.status) }}</span>
              </td>
              <td class="action-col">
                <button
                  v-if="row.status === 'pending'"
                  class="text-action danger"
                  @click="handleCancel(row)"
                >
                  取消
                </button>
                <span v-else class="muted">—</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p class="table-empty" v-if="loading">加载中…</p>
        <p class="table-empty" v-else-if="reservations.length === 0">还没有预约，左侧提交第一条。</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import labApi from '@/api/lab'
import reservationApi from '@/api/reservation'

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const labs = ref([])
const reservations = ref([])

const form = reactive({
  labId: null,
  timeRange: [],
  people: 1,
  purpose: ''
})

const availableLabs = computed(() => labs.value.filter((lab) => lab.status === 'available'))

const rules = {
  labId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择预约时间段', trigger: 'change' }],
  people: [{ required: true, message: '请填写使用人数', trigger: 'blur' }]
}

function disabledDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function shortDate(value) {
  return value ? value.slice(0, 10) : '—'
}

function shortClock(value) {
  return value ? value.slice(11, 16) : '—'
}

function statusText(status) {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[status] || status
}

function pillClass(status) {
  return {
    pending: 'pill-warning',
    approved: 'pill-positive',
    rejected: 'pill-negative'
  }[status] || 'pill-neutral'
}

async function loadLabs() {
  const res = await labApi.list()
  labs.value = res.data || []
}

async function loadMyReservations() {
  loading.value = true
  try {
    const res = await reservationApi.my()
    reservations.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.labId = null
  form.timeRange = []
  form.people = 1
  form.purpose = ''
  formRef.value && formRef.value.clearValidate()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    await reservationApi.submit({
      labId: form.labId,
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      people: form.people,
      purpose: form.purpose
    })
    ElMessage.success('预约已提交，等待审核')
    resetForm()
    await loadMyReservations()
  } catch (e) {
    // 冲突与超员提示由拦截器统一展示
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('取消后该时段将释放，确认取消？', '取消预约', {
      confirmButtonText: '取消预约',
      cancelButtonText: '保留',
      type: 'warning'
    })
    await reservationApi.cancel(row.id)
    ElMessage.success('已取消预约')
    await loadMyReservations()
  } catch (e) {
    // 用户取消或请求失败
  }
}

onMounted(async () => {
  await loadLabs()
  await loadMyReservations()
})
</script>

<style scoped>
.res-grid {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: var(--gap);
  align-items: start;
}

.form-panel {
  padding-bottom: 20px;
}

.submit-btn {
  width: calc(100% - 76px);
}

.reset-btn {
  margin-left: 8px;
}

.action-col {
  width: 80px;
  text-align: right;
}

.muted {
  color: var(--muted);
}

@media (max-width: 1100px) {
  .res-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
