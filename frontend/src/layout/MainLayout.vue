<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <span class="brand-text">社团管理</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" exact-active-class="">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <span>回到主页</span>
        </router-link>
        <router-link to="/admin/dashboard" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="7" height="7" rx="1.5"/>
            <rect x="14" y="3" width="7" height="7" rx="1.5"/>
            <rect x="3" y="14" width="7" height="7" rx="1.5"/>
            <rect x="14" y="14" width="7" height="7" rx="1.5"/>
          </svg>
          <span>首页概览</span>
        </router-link>

        <!-- 管理员专属：社团管理、活动管理 -->
        <router-link v-if="userStore.isAdmin" to="/admin/clubs" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <span>社团管理</span>
        </router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/activities" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <span>活动管理</span>
        </router-link>

        <!-- 普通用户：我的社团、我的活动 -->
        <router-link v-if="!userStore.isAdmin" to="/admin/my-clubs" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <span>我的社团</span>
        </router-link>
        <router-link v-if="!userStore.isAdmin" to="/admin/my-activities" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <span>我的活动</span>
        </router-link>

        <router-link to="/admin/registrations" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <polyline points="9 11 12 14 22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
          <span>报名签到</span>
        </router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/statistics" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <line x1="18" y1="20" x2="18" y2="10"/>
            <line x1="12" y1="20" x2="12" y2="4"/>
            <line x1="6" y1="20" x2="6" y2="14"/>
            <line x1="3" y1="20" x2="21" y2="20"/>
          </svg>
          <span>数据统计</span>
        </router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/ai-reports" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a4 4 0 0 1 4 4c0 1.5-.8 2.8-2 3.5v1.5h-4v-1.5A4 4 0 0 1 12 2z"/>
            <path d="M8 11h8v2a4 4 0 0 1-8 0v-2z"/>
            <line x1="12" y1="17" x2="12" y2="20"/>
            <line x1="8" y1="20" x2="16" y2="20"/>
          </svg>
          <span>AI 分析</span>
        </router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/ai-chat" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            <line x1="9" y1="10" x2="9" y2="10"/>
            <line x1="12" y1="10" x2="12" y2="10"/>
            <line x1="15" y1="10" x2="15" y2="10"/>
          </svg>
          <span>AI 助手</span>
        </router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/users" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            <line x1="19" y1="8" x2="19" y2="14"/>
            <line x1="22" y1="11" x2="16" y2="11"/>
          </svg>
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/messages" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
          <span>站内信</span>
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </router-link>
        <router-link to="/admin/profile" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <span>个人中心</span>
        </router-link>
      </nav>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="topbar-right">
          <router-link to="/admin/messages" class="bell-btn" title="站内信">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
          <div class="user-badge">
            <div class="user-avatar">{{ avatarText }}</div>
            <span class="user-name">{{ userStore.userInfo?.name }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/message'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

onMounted(async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) { /* ignore */ }
})

const avatarText = computed(() => {
  const name = userStore.userInfo?.name || ''
  return name.charAt(name.length - 1) || 'U'
})

const pageTitle = computed(() => {
  const map = {
    '/admin/dashboard': '首页概览',
    '/admin/clubs': '社团管理',
    '/admin/activities': '活动管理',
    '/admin/my-clubs': '我的社团',
    '/admin/my-activities': '我的活动',
    '/admin/messages': '站内信',
    '/admin/registrations': '报名签到',
    '/admin/statistics': '数据统计',
    '/admin/ai-reports': 'AI 分析报告',
    '/admin/ai-chat': 'AI 助手',
    '/admin/users': '用户管理',
    '/admin/profile': '个人中心'
  }
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path)) return title
  }
  return '社团管理系统'
})

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid var(--color-border);
  padding: 20px 12px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 24px;
}

.brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
}

.brand-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  color: var(--color-text-light);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.15s ease;
  text-decoration: none;
}

.nav-item:hover {
  background: var(--color-primary-lighter);
  color: var(--color-primary);
}

.nav-item.active {
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  font-weight: 600;
}

.badge {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--color-danger, #e53e3e);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-btn {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: var(--color-text-light);
  transition: all 0.15s;
  text-decoration: none;
}

.bell-btn:hover {
  background: var(--color-primary-lighter);
  color: var(--color-primary);
}

.bell-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--color-danger, #e53e3e);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.nav-item svg {
  flex-shrink: 0;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: #fff;
  border-bottom: 1px solid var(--color-border);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: var(--color-text);
  font-weight: 500;
}

.logout-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-light);
  transition: all 0.15s;
}

.logout-btn:hover {
  background: rgba(229, 62, 62, 0.06);
  color: var(--color-danger);
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  background: var(--color-background);
}
</style>
