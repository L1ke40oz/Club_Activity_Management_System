<template>
  <div class="registration-page">
    <div class="page-header">
      <div>
        <h3>{{ userStore.isAdmin ? '全部报名签到' : '报名签到' }}</h3>
        <p class="page-desc">{{ userStore.isAdmin ? '查看所有用户的活动报名和签到情况' : '查看我的活动报名记录，进行签到和取消报名' }}</p>
      </div>
    </div>

    <div class="reg-card">
      <el-table :data="registrations" v-loading="loading" style="width: 100%">
        <el-table-column v-if="userStore.isAdmin" prop="studentId" label="学号" width="120" />
        <el-table-column label="活动名称" min-width="160">
          <template #default="{ row }">
            <span class="activity-name">{{ row.activityTitle || '加载中...' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="actStatusType(row.activityStatus)" size="small" effect="light">
              {{ actStatusText(row.activityStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">{{ formatTime(row.regTime) }}</template>
        </el-table-column>
        <el-table-column label="签到状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getSignStatusClass(row)]">
              {{ getSignStatusText(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column v-if="!userStore.isAdmin" label="操作" width="150" align="center">
          <template #default="{ row }">
            <!-- 活动进行中 + 未签到：可签到/取消 -->
            <template v-if="row.activityStatus === 1 && row.signInStatus === 0">
              <el-button size="small" type="success" @click="handleSignin(row)">签到</el-button>
              <el-button size="small" type="danger" text @click="handleCancel(row)">取消</el-button>
            </template>
            <!-- 已签到 -->
            <span v-else-if="row.signInStatus === 1" class="done-text">✓ 已完成</span>
            <!-- 活动已结束 + 未签到 -->
            <span v-else-if="row.activityStatus === 2 && row.signInStatus === 0" class="fail-text">✗ 未完成</span>
            <!-- 活动待审批 -->
            <span v-else class="wait-text">等待中</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && registrations.length === 0" class="empty-state">
        <p>暂无报名记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyRegistrations, getAllRegistrations, cancelRegistration, signin } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const registrations = ref([])
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function actStatusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info'
}

function actStatusText(s) {
  return { 0: '待审批', 1: '进行中', 2: '已结束' }[s] || '未知'
}

function getSignStatusClass(row) {
  if (row.signInStatus === 1) return 'signed'
  if (row.activityStatus === 2) return 'no-show'
  return 'pending'
}

function getSignStatusText(row) {
  if (row.signInStatus === 1) return '已签到'
  if (row.activityStatus === 2) return '未签到'
  return '待签到'
}

async function loadData() {
  loading.value = true
  try {
    let res
    if (userStore.isAdmin) {
      res = await getAllRegistrations()
    } else {
      res = await getMyRegistrations()
    }
    registrations.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleSignin(row) {
  try {
    await signin(row.id)
    ElMessage.success('签到成功！')
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定要取消报名该活动吗？', '提示', { type: 'warning' })
  try {
    await cancelRegistration(row.id)
    ElMessage.success('已取消报名')
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.registration-page {
  max-width: 1000px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--color-text-light);
}

.reg-card {
  background: #fff;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  padding: 20px;
}

.activity-name {
  font-weight: 500;
  color: var(--color-text);
}

.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.signed {
  background: rgba(56, 161, 105, 0.08);
  color: var(--color-accent);
}

.status-tag.pending {
  background: rgba(43, 108, 176, 0.08);
  color: var(--color-primary);
}

.status-tag.no-show {
  background: rgba(229, 62, 62, 0.08);
  color: var(--color-danger);
}

.done-text {
  font-size: 13px;
  color: var(--color-accent);
  font-weight: 500;
}

.fail-text {
  font-size: 13px;
  color: var(--color-danger);
  font-weight: 500;
}

.wait-text {
  font-size: 13px;
  color: var(--color-text-light);
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  color: var(--color-text-light);
}
</style>
