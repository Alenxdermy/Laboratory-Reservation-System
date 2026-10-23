<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>运行概览</h1>
        <p class="title-sub" v-if="weekRange">
          本周 {{ weekRange }} · 在册 {{ labCount }} 间实验室、{{ deviceCount }} 台设备
        </p>
      </div>
      <div class="title-actions">
        <el-button
          v-if="userStore.isManager"
          type="primary"
          @click="router.push('/audit')"
        >
          审核预约<template v-if="pendingCount > 0">（{{ pendingCount }}）</template>
        </el-button>
        <el-button v-else type="primary" @click="router.push('/reservations')">
          新建预约
        </el-button>
      </div>
    </div>

    <!-- KPI：一个容器，1px 分隔 -->
    <div class="kpi-strip">
      <div class="kpi-cell">
        <span class="kpi-label">实验室</span>
        <div class="kpi-value">{{ labCount }}<span class="kpi-unit">间</span></div>
        <span class="kpi-note">可用 {{ availableLabCount }} 间</span>
      </div>
      <div class="kpi-cell">
        <span class="kpi-label">设备</span>
        <div class="kpi-value">{{ deviceCount }}<span class="kpi-unit">台</span></div>
        <span class="kpi-note">维修中 {{ maintenanceDeviceCount }} 台</span>
      </div>
      <div class="kpi-cell">
        <span class="kpi-label">本周预约</span>
        <div class="kpi-value">{{ weeklyTotal }}<span class="kpi-unit">次</span></div>
        <span class="kpi-note">全部实验室合计</span>
      </div>
      <div class="kpi-cell" v-if="userStore.isManager">
        <span class="kpi-label">待审核</span>
        <div class="kpi-value">{{ pendingCount }}<span class="kpi-unit">条</span></div>
        <span class="kpi-note">需要处理</span>
      </div>
      <div class="kpi-cell" v-else>
        <span class="kpi-label">我的预约</span>
        <div class="kpi-value">{{ myReservationCount }}<span class="kpi-unit">条</span></div>
        <span class="kpi-note">含历史记录</span>
      </div>
    </div>

    <div class="dash-grid">
      <div class="dash-main">
        <section class="panel">
          <div class="panel-head">
            <h2>各实验室本周预约次数</h2>
            <span class="panel-meta" v-if="weekRange">{{ weekRange }}</span>
          </div>
          <div ref="chartRef" class="chart"></div>
          <p class="chart-note" v-if="!chartData.labels.length">本周还没有预约，图表保持空坐标系。</p>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h2>周使用率</h2>
            <span class="panel-meta">已通过预约的累计时长 ÷ 每周开放 84 小时</span>
          </div>
          <div class="bar-list" v-if="details.length">
            <div class="bar-item" v-for="item in details" :key="item.labId">
              <span class="bar-name">{{ item.labName }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: Math.min(item.rate, 100) + '%' }"></div>
              </div>
              <span class="bar-value">{{ item.rate.toFixed(1) }}%</span>
            </div>
          </div>
          <p class="empty-note" v-else>暂无实验室数据。</p>
        </section>
      </div>

      <aside class="dash-rail">
        <h2 class="rail-head">最近预约</h2>
        <div class="rail-list" v-if="recent.length">
          <div class="rail-row" v-for="item in recent" :key="item.id">
            <div class="rail-main">
              <div class="rail-title">{{ item.labName }}</div>
              <div class="rail-sub">{{ shortTime(item.startTime) }} · {{ item.username }}</div>
            </div>
            <span class="pill" :class="pillClass(item.status)">{{ statusText(item.status) }}</span>
          </div>
        </div>
        <p class="empty-note" v-else>还没有预约记录。</p>
        <el-button class="rail-more" @click="router.push(userStore.isManager ? '/audit' : '/reservations')">
          查看全部
        </el-button>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import labApi from '@/api/lab'
import deviceApi from '@/api/device'
import reservationApi from '@/api/reservation'
import statApi from '@/api/stat'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const chartRef = ref(null)
let chart = null

const labCount = ref(0)
const availableLabCount = ref(0)
const deviceCount = ref(0)
const maintenanceDeviceCount = ref(0)
const myReservationCount = ref(0)
const pendingCount = ref(0)
const weeklyTotal = ref(0)
const weekRange = ref('')
const recent = ref([])
const details = ref([])

const chartData = ref({ labels: [], values: [] })

const canSeeAll = computed(() => userStore.isManager)

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

function shortTime(value) {
  if (!value) return ''
  // 2026-10-26 09:00:00 -> 10-26 09:00
  return value.slice(5, 16)
}

async function loadData() {
  const tasks = [labApi.list(), deviceApi.list(), statApi.weeklyUsage()]
  if (canSeeAll.value) {
    tasks.push(reservationApi.list({}))
  } else {
    tasks.push(reservationApi.my())
  }
  const [labRes, deviceRes, statRes, resRes] = await Promise.all(tasks)

  const labs = labRes.data || []
  const devices = deviceRes.data || []
  const reservations = resRes.data || []

  labCount.value = labs.length
  availableLabCount.value = labs.filter((l) => l.status === 'available').length
  deviceCount.value = devices.length
  maintenanceDeviceCount.value = devices.filter((d) => d.status === 'maintenance').length
  myReservationCount.value = reservations.length
  pendingCount.value = canSeeAll.value
    ? reservations.filter((r) => r.status === 'pending').length
    : 0
  recent.value = reservations.slice(0, 5)

  const data = statRes.data || {}
  chartData.value = { labels: data.labels || [], values: data.values || [] }
  details.value = data.details || []
  weeklyTotal.value = (data.values || []).reduce((sum, v) => sum + Number(v || 0), 0)
  if (data.startDate) {
    weekRange.value = `${data.startDate} 至 ${data.endDate}`
  }

  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    animation: false,
    grid: { left: 8, right: 12, top: 16, bottom: 8, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#ffffff',
      borderColor: '#e8eaec',
      textStyle: { color: '#131416', fontSize: 12 }
    },
    xAxis: {
      type: 'category',
      data: chartData.value.labels,
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: {
        color: '#5d5f60',
        fontSize: 12,
        interval: 0,
        formatter: (value) => (value.length > 7 ? value.slice(0, 7) + '…' : value)
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#5d5f60', fontSize: 12 },
      splitLine: { lineStyle: { color: '#e5e5e5', width: 1 } }
    },
    series: [
      {
        name: '预约次数',
        type: 'bar',
        data: chartData.value.values,
        barMaxWidth: 28,
        itemStyle: { color: '#1b6c80', borderRadius: [3, 3, 0, 0] }
      }
    ]
  }, true)
}

function handleResize() {
  if (chart) chart.resize()
}

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 384px;
  gap: var(--gap);
  align-items: start;
}

.dash-main {
  display: flex;
  flex-direction: column;
  gap: var(--gap);
  min-width: 0;
}

.chart {
  width: 100%;
  height: 300px;
}

.chart-note,
.empty-note {
  margin: 12px 0 0;
  font-size: var(--fs-small);
  color: var(--muted);
}

.rail-head {
  margin: 0 0 8px;
  font-size: var(--fs-small);
  font-weight: 500;
  color: var(--muted);
}

.dash-rail {
  min-width: 0;
}

.rail-more {
  width: 100%;
  height: 30px;
  margin-top: 12px;
}

.bar-name {
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 1100px) {
  .dash-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .dash-rail {
    padding-top: 8px;
  }
}

@media (max-width: 900px) {
  .chart {
    height: 220px;
  }
}
</style>
