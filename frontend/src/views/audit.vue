<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>预约审核</h1>
        <p class="title-sub">
          当前筛选下 {{ list.length }} 条，待审核 {{ pendingCount }} 条
        </p>
      </div>
      <div class="title-actions">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="loadList">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-select v-model="query.labId" placeholder="全部实验室" clearable style="width: 160px" @change="loadList">
          <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
        </el-select>
        <el-input
          v-model.trim="query.keyword"
          placeholder="搜索预约人或实验室"
          clearable
          style="width: 190px"
          @keyup.enter="loadList"
          @clear="loadList"
        />
      </div>
    </div>

    <div class="panel">
      <table class="data-table">
        <thead>
          <tr>
            <th>预约人</th>
            <th>实验室</th>
            <th>时间段</th>
            <th class="num">人数 / 容量</th>
            <th>状态</th>
            <th class="action-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.id">
            <td>
              <span>{{ row.username }}</span>
              <span class="secondary">{{ row.purpose || '未填写用途' }}</span>
            </td>
            <td>{{ row.labName }}</td>
            <td>
              <span>{{ shortDate(row.startTime) }}</span>
              <span class="secondary mono">{{ shortClock(row.startTime) }} – {{ shortClock(row.endTime) }}</span>
            </td>
            <td class="num">{{ row.people }} / {{ row.labCapacity }}</td>
            <td>
              <span class="pill" :class="pillClass(row.status)">{{ statusText(row.status) }}</span>
            </td>
            <td class="action-col">
              <template v-if="row.status === 'pending'">
                <button class="text-action" @click="handleAudit(row, 'approved')">通过</button>
                <button class="text-action danger" @click="handleAudit(row, 'rejected')">拒绝</button>
              </template>
              <span v-else class="muted">已处理</span>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="table-empty" v-if="loading">加载中…</p>
      <p class="table-empty" v-else-if="list.length === 0">当前筛选下没有预约记录。</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import reservationApi from '@/api/reservation'
import labApi from '@/api/lab'

const list = ref([])
const labs = ref([])
const loading = ref(false)
const query = reactive({ status: 'pending', labId: null, keyword: '' })

const pendingCount = computed(() => list.value.filter((r) => r.status === 'pending').length)

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

async function loadList() {
  loading.value = true
  try {
    const res = await reservationApi.list({
      status: query.status || undefined,
      labId: query.labId || undefined,
      keyword: query.keyword || undefined
    })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleAudit(row, status) {
  const action = status === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(
      `确认${action}「${row.username}」对「${row.labName}」的预约？`,
      `${action}预约`,
      { confirmButtonText: action, cancelButtonText: '取消', type: 'warning' }
    )
    await reservationApi.audit(row.id, status)
    ElMessage.success(`已${action}该预约`)
    await loadList()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(async () => {
  await loadLabs()
  await loadList()
})
</script>

<style scoped>
.action-col {
  width: 110px;
  text-align: right;
  white-space: nowrap;
}

.muted {
  color: var(--muted);
}

@media (max-width: 700px) {
  .panel {
    overflow-x: auto;
  }

  .data-table {
    min-width: 640px;
  }
}
</style>
