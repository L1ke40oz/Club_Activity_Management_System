<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>{{ greeting }}，{{ userStore.userInfo?.name || '同学' }}</h1>
        <p v-if="userStore.isAdmin">系统管理概览，查看待处理事项和整体运行状况</p>
        <p v-else>查看你参与的社团和活动动态</p>
      </div>
    </div>

    <!-- 管理员视图 -->
    <template v-if="userStore.isAdmin">
      <div class="stats-grid stats-grid--4">
        <div class="stat-card stat-clubs">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.clubCount }}</span>
            <span class="stat-label">社团总数</span>
          </div>
        </div>
        <div class="stat-card stat-activities">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.activityCount }}</span>
            <span class="stat-label">活动总数</span>
          </div>
        </div>
        <div class="stat-card stat-users">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.userCount }}</span>
            <span class="stat-label">注册用户</span>
          </div>
        </div>
        <div class="stat-card stat-pending">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.pendingCount }}</span>
            <span class="stat-label">待审批</span>
          </div>
        </div>
      </div>

      <div class="content-grid">
        <div class="panel">
          <div class="panel-header">
            <h3>待审批活动</h3>
            <router-link to="/admin/activities" class="panel-link">查看全部</router-link>
          </div>
          <div class="panel-body">
            <div v-if="pendingActivities.length === 0" class="empty-state">暂无待审批活动</div>
            <div v-for="item in pendingActivities" :key="item.activityId" class="activity-item">
              <div class="activity-info">
                <span class="activity-title">{{ item.title }}</span>
                <span class="activity-meta">{{ item.location || '未设置地点' }} · {{ formatTime(item.eventTime) }}</span>
              </div>
              <span class="status-badge status-0">待审批</span>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <h3>社团概览</h3>
            <router-link to="/admin/clubs" class="panel-link">查看全部</router-link>
          </div>
          <div class="panel-body">
            <div v-if="clubs.length === 0" class="empty-state">暂无社团数据</div>
            <div v-for="item in clubs" :key="item.clubId" class="club-item">
              <div class="club-avatar">{{ item.name?.charAt(0) }}</div>
              <div class="club-info">
                <span class="club-name">{{ item.name }}</span>
                <span class="club-meta">{{ item.category || '未分类' }} · {{ item.advisor || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 普通用户视图 -->
    <template v-else>
      <div class="stats-grid stats-grid--3">
        <div class="stat-card stat-clubs">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.clubCount }}</span>
            <span class="stat-label">全校社团</span>
          </div>
        </div>
        <div class="stat-card stat-activities">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.ongoingCount }}</span>
            <span class="stat-label">进行中活动</span>
          </div>
        </div>
        <div class="stat-card stat-pending">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.myRegCount }}</span>
            <span class="stat-label">我的报名</span>
          </div>
        </div>
      </div>

      <div class="content-grid">
        <div class="panel">
          <div class="panel-header">
            <h3>可报名活动</h3>
            <router-link to="/explore/activities" class="panel-link">查看全部</router-link>
          </div>
          <div class="panel-body">
            <div v-if="publishedActivities.length === 0" class="empty-state">暂无可报名活动</div>
            <div v-for="item in publishedActivities" :key="item.activityId" class="activity-item">
              <div class="activity-info">
                <span class="activity-title">{{ item.title }}</span>
                <span class="activity-meta">{{ item.location || '未设置地点' }} · {{ formatTime(item.eventTime) }}</span>
              </div>
              <span class="status-badge status-1">可报名</span>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <h3>我的报名记录</h3>
            <router-link to="/admin/registrations" class="panel-link">查看全部</router-link>
          </div>
          <div class="panel-body">
            <div v-if="myRegistrations.length === 0" class="empty-state">还没有报名任何活动</div>
            <div v-for="item in myRegistrations" :key="item.id" class="activity-item">
              <div class="activity-info">
                <span class="activity-title">{{ item.activityTitle || `活动 #${item.activityId}` }}</span>
                <span class="activity-meta">{{ formatTime(item.regTime) }}</span>
              </div>
              <span :class="['status-badge', item.signInStatus === 1 ? 'status-1' : 'status-2']">
                {{ item.signInStatus === 1 ? '已签到' : '未签到' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getClubList } from '@/api/club'
import { getActivityList, getActivityDetail } from '@/api/activity'
import { getUserList } from '@/api/user'
import { getMyRegistrations } from '@/api/registration'

const userStore = useUserStore()
const stats = reactive({ clubCount: 0, activityCount: 0, userCount: 0, pendingCount: 0, ongoingCount: 0, myRegCount: 0 })
const pendingActivities = ref([])
const publishedActivities = ref([])
const clubs = ref([])
const myRegistrations = ref([])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  try {
    if (userStore.isAdmin) {
      const [clubRes, actRes, userRes] = await Promise.all([
        getClubList(),
        getActivityList(),
        getUserList()
      ])
      clubs.value = (clubRes.data || []).slice(0, 5)
      const allActivities = actRes.data || []
      pendingActivities.value = allActivities.filter(a => a.status === 0).slice(0, 5)
      stats.clubCount = (clubRes.data || []).length
      stats.activityCount = allActivities.length
      stats.userCount = (userRes.data || []).length
      stats.pendingCount = pendingActivities.value.length
    } else {
      const [clubRes, actRes, regRes] = await Promise.all([
        getClubList(),
        getActivityList(),
        getMyRegistrations()
      ])
      const allActivities = actRes.data || []
      publishedActivities.value = allActivities.filter(a => a.status === 1).slice(0, 5)
      const regList = (regRes.data || []).slice(0, 5)
      // 补充活动标题（如果后端没有返回）
      for (const reg of regList) {
        if (!reg.activityTitle) {
          try {
            const actDetail = await getActivityDetail(reg.activityId)
            reg.activityTitle = actDetail.data?.title || `活动 #${reg.activityId}`
          } catch (e) {
            reg.activityTitle = `活动 #${reg.activityId}`
          }
        }
      }
      myRegistrations.value = regList
      stats.clubCount = (clubRes.data || []).length
      stats.ongoingCount = allActivities.filter(a => a.status === 1).length
      stats.myRegCount = (regRes.data || []).length
    }
  } catch (e) {
    // silently fail
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1100px;
}

.welcome-section {
  margin-bottom: 28px;
}

.welcome-text h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
}

.welcome-text p {
  font-size: 14px;
  color: var(--color-text-light);
}

.stats-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 28px;
}

.stats-grid--4 {
  grid-template-columns: repeat(4, 1fr);
}

.stats-grid--3 {
  grid-template-columns: repeat(3, 1fr);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: var(--radius-md);
  background: #fff;
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-clubs .stat-icon { background: rgba(43, 108, 176, 0.08); color: var(--color-primary); }
.stat-activities .stat-icon { background: rgba(56, 161, 105, 0.08); color: var(--color-accent); }
.stat-users .stat-icon { background: rgba(221, 107, 32, 0.08); color: var(--color-warning); }
.stat-pending .stat-icon { background: rgba(229, 62, 62, 0.08); color: var(--color-danger); }

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-light);
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.panel {
  background: #fff;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.panel-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.panel-link {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.panel-link:hover {
  text-decoration: underline;
}

.panel-body {
  padding: 8px 12px;
}

.empty-state {
  padding: 32px 0;
  text-align: center;
  color: var(--color-text-light);
  font-size: 14px;
}

.activity-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
  border-radius: 8px;
  transition: background 0.15s;
}

.activity-item:hover {
  background: var(--color-background);
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.activity-meta {
  font-size: 12px;
  color: var(--color-text-light);
}

.status-badge {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 6px;
  font-weight: 500;
  flex-shrink: 0;
}

.status-0 { background: rgba(221, 107, 32, 0.08); color: var(--color-warning); }
.status-1 { background: rgba(56, 161, 105, 0.08); color: var(--color-accent); }
.status-2 { background: rgba(113, 128, 150, 0.08); color: var(--color-text-light); }
.status-3 { background: rgba(229, 62, 62, 0.08); color: var(--color-danger); }

.club-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  transition: background 0.15s;
}

.club-item:hover {
  background: var(--color-background);
}

.club-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.club-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.club-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.club-meta {
  font-size: 12px;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .stats-grid--4,
  .stats-grid--3 {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
