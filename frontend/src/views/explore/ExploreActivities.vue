<template>
  <div class="explore-page">
    <header class="explore-header">
      <div class="container explore-header__inner">
        <router-link to="/" class="back-btn">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          返回主页
        </router-link>
        <h1>全部活动</h1>
        <div class="explore-header__actions">
          <select v-model="filterStatus" @change="filterActivities">
            <option value="">全部状态</option>
            <option value="1">报名中</option>
            <option value="2">已结束</option>
          </select>
          <input v-model="searchTitle" type="text" placeholder="搜索活动..." @input="filterActivities" />
        </div>
      </div>
    </header>

    <main class="container explore-main">
      <div class="activity-grid">
        <div v-for="act in filteredActivities" :key="act.activityId" class="activity-card">
          <div class="activity-card__status" :class="statusClass(act.status)">
            {{ statusText(act.status) }}
          </div>
          <strong class="activity-card__title">{{ act.title }}</strong>
          <p class="activity-card__meta">
            <span>🏛️ {{ act.clubName || '未知社团' }}</span>
            <span>📍 {{ act.location || '待定' }}</span>
            <span>📅 {{ formatDate(act.eventTime) }}</span>
            <span>👥 上限 {{ act.maxParticipants }} 人</span>
          </p>
          <p class="activity-card__desc">{{ act.details || '暂无描述' }}</p>
          <div class="activity-card__actions">
            <button
              v-if="userStore.isLoggedIn && act.status === 1"
              class="btn-signup"
              @click="handleSignup(act.activityId)"
            >
              立即报名
            </button>
            <button v-if="!userStore.isLoggedIn && act.status === 1" class="btn-login" @click="goLogin">
              登录后报名
            </button>
          </div>
        </div>
      </div>

      <div v-if="filteredActivities.length === 0 && !loading" class="empty-hint">
        {{ searchTitle || filterStatus ? '没有找到匹配的活动' : '暂无活动数据' }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getActivityList } from '@/api/activity'
import { signup } from '@/api/registration'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const activities = ref([])
const searchTitle = ref('')
const filterStatus = ref('')
const loading = ref(false)

const filteredActivities = computed(() => {
  let list = activities.value
  if (filterStatus.value) {
    list = list.filter(a => a.status === parseInt(filterStatus.value))
  }
  if (searchTitle.value.trim()) {
    const keyword = searchTitle.value.trim().toLowerCase()
    list = list.filter(a => a.title?.toLowerCase().includes(keyword))
  }
  return list
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getActivityList()
    // 只显示已发布和已结束的（不显示待审批和已驳回的）
    activities.value = (res.data || []).filter(a => a.status === 1 || a.status === 2)
  } finally {
    loading.value = false
  }
})

function filterActivities() {
  // filtering is done by computed
}

function statusClass(status) {
  const map = { 0: 'pending', 1: 'active', 2: 'finished' }
  return map[status] || ''
}

function statusText(status) {
  const map = { 0: '待审批', 1: '报名中', 2: '已结束' }
  return map[status] || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return '待定'
  return dateStr.replace('T', ' ').substring(0, 16)
}

async function handleSignup(activityId) {
  try {
    await signup(activityId)
    ElMessage.success('报名成功！')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '报名失败')
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.explore-page {
  min-height: 100vh;
  background: var(--color-background, #f7fafc);
}

.explore-header {
  background: #fff;
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.explore-header__inner {
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text-light);
  transition: background 0.2s, color 0.2s;
  text-decoration: none;
}

.back-btn:hover {
  background: var(--color-primary-lighter);
  color: var(--color-primary);
}

.explore-header__inner h1 {
  flex: 1;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.explore-header__actions {
  display: flex;
  gap: 10px;
}

.explore-header__actions input,
.explore-header__actions select {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  font-size: 14px;
  color: var(--color-text);
}

.explore-header__actions input {
  width: 180px;
}

.explore-header__actions input:focus,
.explore-header__actions select:focus {
  outline: 2px solid rgba(43, 108, 176, 0.2);
  border-color: var(--color-primary);
}

.container {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
}

.explore-main {
  padding: 32px 0 48px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

.activity-card {
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(43, 108, 176, 0.1);
}

.activity-card__status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 10px;
}

.activity-card__status.active {
  background: rgba(56, 161, 105, 0.1);
  color: #38a169;
}

.activity-card__status.pending {
  background: rgba(221, 107, 32, 0.1);
  color: #dd6b20;
}

.activity-card__status.finished {
  background: rgba(113, 128, 150, 0.1);
  color: #718096;
}

.activity-card__title {
  display: block;
  font-size: 17px;
  color: var(--color-text);
  margin-bottom: 10px;
}

.activity-card__meta {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--color-text-light);
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.activity-card__desc {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-card__actions {
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.btn-signup {
  padding: 8px 20px;
  border-radius: 10px;
  background: var(--color-accent, #38a169);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}

.btn-signup:hover {
  background: var(--color-accent-soft, #48bb78);
  transform: scale(1.03);
}

.btn-login {
  padding: 8px 20px;
  border-radius: 10px;
  background: var(--color-primary, #2b6cb0);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-login:hover {
  background: var(--color-primary-soft, #3182ce);
}

.empty-hint {
  text-align: center;
  padding: 60px;
  color: var(--color-text-light);
  font-size: 15px;
}

@media (max-width: 768px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }
  .explore-header__inner {
    flex-wrap: wrap;
    padding: 12px 0;
  }
  .explore-header__actions {
    width: 100%;
  }
  .explore-header__actions input {
    flex: 1;
  }
}
</style>
