<template>
  <div class="statistics-page">
    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.clubCount || 0 }}</div>
        <div class="stat-card__label">活跃社团</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.memberCount || 0 }}</div>
        <div class="stat-card__label">在籍成员</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.activityCount || 0 }}</div>
        <div class="stat-card__label">活动总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.registrationCount || 0 }}</div>
        <div class="stat-card__label">报名总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.attendanceRate || 0 }}%</div>
        <div class="stat-card__label">总出勤率</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ overview.userCount || 0 }}</div>
        <div class="stat-card__label">注册用户</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-box">
        <h3 class="chart-title">社团成员规模排行</h3>
        <div ref="clubMemberChart" class="chart-canvas"></div>
      </div>
      <div class="chart-box">
        <h3 class="chart-title">社团活动数量排行</h3>
        <div ref="clubActivityChart" class="chart-canvas"></div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-box">
        <h3 class="chart-title">各社团出勤率</h3>
        <div ref="attendanceChart" class="chart-canvas"></div>
      </div>
      <div class="chart-box">
        <h3 class="chart-title">活动状态分布</h3>
        <div ref="activityStatusChart" class="chart-canvas"></div>
      </div>
    </div>

    <!-- 社团详细统计表 -->
    <div class="table-section">
      <div class="table-header">
        <h3 class="section-title">社团综合统计</h3>
        <div class="table-filters">
          <el-input v-model="clubFilter.name" placeholder="搜索社团名称" clearable style="width: 180px" @clear="loadClubStats" @keyup.enter="loadClubStats" />
          <el-input v-model="clubFilter.category" placeholder="分类" clearable style="width: 120px" @clear="loadClubStats" @keyup.enter="loadClubStats" />
          <el-button type="primary" @click="loadClubStats">查询</el-button>
        </div>
      </div>
      <el-table :data="clubStats" stripe v-loading="clubLoading" class="custom-table">
        <el-table-column prop="name" label="社团名称" width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="advisor" label="指导老师" width="100" />
        <el-table-column prop="memberCount" label="成员数" width="80" sortable />
        <el-table-column prop="activityCount" label="活动数" width="80" sortable />
        <el-table-column prop="finishedActivityCount" label="已完成" width="80" sortable />
        <el-table-column prop="totalRegistrations" label="报名数" width="80" sortable />
        <el-table-column prop="totalSignIns" label="签到数" width="80" sortable />
        <el-table-column label="出勤率" width="100" sortable sort-by="attendanceRate">
          <template #default="{ row }">
            <el-progress :percentage="row.attendanceRate" :stroke-width="14" :text-inside="true"
              :color="row.attendanceRate >= 80 ? '#67c23a' : row.attendanceRate >= 60 ? '#e6a23c' : '#f56c6c'" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 成员活跃度统计 -->
    <div class="table-section">
      <div class="table-header">
        <h3 class="section-title">成员活跃度统计</h3>
        <div class="table-filters">
          <el-select v-model="memberFilter.clubId" placeholder="选择社团" clearable style="width: 180px" @change="loadMemberStats">
            <el-option v-for="c in clubOptions" :key="c.clubId" :label="c.name" :value="c.clubId" />
          </el-select>
        </div>
      </div>
      <el-table :data="memberStats" stripe v-loading="memberLoading" class="custom-table">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="clubCount" label="加入社团数" width="100" sortable />
        <el-table-column prop="registrationCount" label="报名次数" width="100" sortable />
        <el-table-column prop="signInCount" label="签到次数" width="100" sortable />
        <el-table-column label="出勤率" width="100" sortable sort-by="attendanceRate">
          <template #default="{ row }">
            <el-progress :percentage="row.attendanceRate" :stroke-width="14" :text-inside="true"
              :color="row.attendanceRate >= 80 ? '#67c23a' : row.attendanceRate >= 60 ? '#e6a23c' : '#f56c6c'" />
          </template>
        </el-table-column>
        <el-table-column prop="noShowCount" label="缺勤次数" width="90" sortable />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getClubStats, getMemberActivityStats } from '@/api/statistics'
import { getClubList } from '@/api/club'

const overview = ref({})
const clubStats = ref([])
const memberStats = ref([])
const clubOptions = ref([])
const clubLoading = ref(false)
const memberLoading = ref(false)

const clubFilter = ref({ name: '', category: '' })
const memberFilter = ref({ clubId: null })

const clubMemberChart = ref(null)
const clubActivityChart = ref(null)
const attendanceChart = ref(null)
const activityStatusChart = ref(null)

let chartInstances = []

onMounted(async () => {
  await Promise.all([loadOverview(), loadClubStats(), loadMemberStats(), loadClubOptions()])
  await nextTick()
  renderCharts()
})

async function loadOverview() {
  try {
    const res = await getOverview()
    overview.value = res.data || {}
  } catch (e) { /* ignore */ }
}

async function loadClubStats() {
  clubLoading.value = true
  try {
    const params = {}
    if (clubFilter.value.name) params.name = clubFilter.value.name
    if (clubFilter.value.category) params.category = clubFilter.value.category
    const res = await getClubStats(params)
    clubStats.value = res.data || []
    await nextTick()
    renderCharts()
  } catch (e) { /* ignore */ }
  clubLoading.value = false
}

async function loadMemberStats() {
  memberLoading.value = true
  try {
    const params = {}
    if (memberFilter.value.clubId) params.clubId = memberFilter.value.clubId
    const res = await getMemberActivityStats(params)
    memberStats.value = res.data || []
  } catch (e) { /* ignore */ }
  memberLoading.value = false
}

async function loadClubOptions() {
  try {
    const res = await getClubList()
    clubOptions.value = res.data || []
  } catch (e) { /* ignore */ }
}

function renderCharts() {
  chartInstances.forEach(c => c.dispose())
  chartInstances = []

  if (clubStats.value.length === 0) return

  const top10 = [...clubStats.value].sort((a, b) => b.memberCount - a.memberCount).slice(0, 10)
  const names = top10.map(c => c.name)

  // 成员规模排行
  if (clubMemberChart.value) {
    const chart = echarts.init(clubMemberChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, fontSize: 11 } },
      yAxis: { type: 'value', name: '人数' },
      series: [{ type: 'bar', data: top10.map(c => c.memberCount), itemStyle: { color: '#5b8ff9', borderRadius: [4, 4, 0, 0] } }],
      grid: { left: 50, right: 20, bottom: 60, top: 30 }
    })
    chartInstances.push(chart)
  }

  // 活动数量排行
  const top10Act = [...clubStats.value].sort((a, b) => b.activityCount - a.activityCount).slice(0, 10)
  if (clubActivityChart.value) {
    const chart = echarts.init(clubActivityChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: top10Act.map(c => c.name), axisLabel: { rotate: 30, fontSize: 11 } },
      yAxis: { type: 'value', name: '活动数' },
      series: [{ type: 'bar', data: top10Act.map(c => c.activityCount), itemStyle: { color: '#5ad8a6', borderRadius: [4, 4, 0, 0] } }],
      grid: { left: 50, right: 20, bottom: 60, top: 30 }
    })
    chartInstances.push(chart)
  }

  // 出勤率
  if (attendanceChart.value) {
    const chart = echarts.init(attendanceChart.value)
    const sorted = [...clubStats.value].filter(c => c.totalRegistrations > 0).sort((a, b) => b.attendanceRate - a.attendanceRate).slice(0, 10)
    chart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
      xAxis: { type: 'category', data: sorted.map(c => c.name), axisLabel: { rotate: 30, fontSize: 11 } },
      yAxis: { type: 'value', name: '%', max: 100 },
      series: [{
        type: 'bar',
        data: sorted.map(c => c.attendanceRate),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => params.value >= 80 ? '#67c23a' : params.value >= 60 ? '#e6a23c' : '#f56c6c'
        }
      }],
      grid: { left: 50, right: 20, bottom: 60, top: 30 }
    })
    chartInstances.push(chart)
  }

  // 活动状态饼图
  if (activityStatusChart.value) {
    const chart = echarts.init(activityStatusChart.value)
    const ov = overview.value
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 10 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        label: { show: true, formatter: '{b}: {c}' },
        data: [
          { name: '进行中', value: ov.publishedActivityCount || 0, itemStyle: { color: '#5b8ff9' } },
          { name: '已结束', value: ov.finishedActivityCount || 0, itemStyle: { color: '#5ad8a6' } },
          { name: '待审批', value: (ov.activityCount || 0) - (ov.publishedActivityCount || 0) - (ov.finishedActivityCount || 0), itemStyle: { color: '#e8684a' } }
        ]
      }]
    })
    chartInstances.push(chart)
  }
}

window.addEventListener('resize', () => {
  chartInstances.forEach(c => c.resize())
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border: 1px solid var(--color-border);
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 6px;
}

.stat-card__label {
  font-size: 13px;
  color: var(--color-text-light);
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-box {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.chart-canvas {
  width: 100%;
  height: 280px;
}

.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.table-filters {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
