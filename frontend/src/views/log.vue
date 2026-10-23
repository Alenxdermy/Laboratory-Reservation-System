<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>操作日志</h1>
        <p class="title-sub">记录谁在什么时间做了什么，按时间倒序展示最近 {{ logs.length }} 条</p>
      </div>
      <div class="title-actions">
        <el-input
          v-model.trim="keyword"
          placeholder="搜索操作人、操作或详情"
          clearable
          style="width: 240px"
          @keyup.enter="loadLogs"
          @clear="loadLogs"
        />
        <el-button @click="loadLogs">刷新</el-button>
      </div>
    </div>

    <div class="panel">
      <table class="data-table">
        <thead>
          <tr>
            <th class="time-col">时间</th>
            <th class="user-col">操作人</th>
            <th class="action-name">操作</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in logs" :key="row.id">
            <td class="time-col mono">{{ row.createTime || '—' }}</td>
            <td class="user-col">{{ row.username || '系统' }}</td>
            <td class="action-name">{{ row.action }}</td>
            <td>{{ row.detail || '—' }}</td>
          </tr>
        </tbody>
      </table>
      <p class="table-empty" v-if="loading">加载中…</p>
      <p class="table-empty" v-else-if="logs.length === 0">没有匹配的日志记录。</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import logApi from '@/api/log'

const logs = ref([])
const keyword = ref('')
const loading = ref(false)

async function loadLogs() {
  loading.value = true
  try {
    const res = await logApi.list({ keyword: keyword.value || undefined, limit: 300 })
    logs.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)
</script>

<style scoped>
.time-col {
  width: 180px;
  white-space: nowrap;
}

.user-col {
  width: 130px;
}

.action-name {
  width: 150px;
  white-space: nowrap;
}
</style>
