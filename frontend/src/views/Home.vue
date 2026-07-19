<template>
  <div class="home-page">
    <!-- 顶部快捷栏 -->
    <div class="top-bar">
      <div class="container top-bar__inner">
        <p class="welcome-text">欢迎来到校园社团活动管理平台</p>
        <nav class="top-bar__nav">
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/login">登录</router-link>
            <router-link to="/login">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/admin/messages" class="msg-link" title="站内信">
              🔔<span v-if="unreadMsgCount > 0" class="msg-badge">{{ unreadMsgCount }}</span>
            </router-link>
            <span class="user-greeting">{{ userStore.userInfo?.name }}</span>
            <router-link to="/admin/dashboard" class="admin-link">进入后台</router-link>
            <a href="#" @click.prevent="handleLogout">退出</a>
          </template>
        </nav>
      </div>
    </div>

    <!-- 站点头部导航 -->
    <header class="site-header">
      <div class="container site-header__inner">
        <div class="site-header__brand">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h2>社团活动平台</h2>
        </div>
        <nav class="site-header__nav">
          <a href="#clubs-section">社团浏览</a>
          <a href="#activities-section">活动大厅</a>
          <a href="#services-section">快捷服务</a>
        </nav>
      </div>
    </header>

    <main>
      <!-- Hero 区域 -->
      <section class="hero-section">
        <div class="container hero-section__inner">
          <div class="hero-section__content">
            <p class="eyebrow">Campus Club · 社团风采</p>
            <h1>发现校园社团，参与精彩活动</h1>
            <p class="hero-section__desc">
              一站式查看全校社团信息、浏览近期活动、在线报名参与，开启丰富的校园社团生活。
            </p>
            <form class="search-panel" @submit.prevent="handleSearch">
              <label class="search-panel__label">社团检索</label>
              <div class="search-panel__controls">
                <input v-model="searchKeyword" type="text" placeholder="输入社团名称搜索..." />
                <button type="submit">搜索</button>
              </div>
              <ul class="search-tags">
                <li v-for="tag in quickTags" :key="tag" @click="quickSearch(tag)">{{ tag }}</li>
              </ul>
            </form>
          </div>
          <aside class="hero-section__panel">
            <div class="hero-card">
              <span>活跃社团</span>
              <strong>{{ stats.clubCount }} 个</strong>
            </div>
            <div class="hero-card">
              <span>进行中活动</span>
              <strong>{{ stats.activeActivityCount }} 场</strong>
            </div>
            <div class="hero-card">
              <span>累计活动</span>
              <strong>{{ stats.totalActivityCount }} 场</strong>
            </div>
          </aside>
        </div>
      </section>

      <!-- 社团浏览 -->
      <section id="clubs-section" class="section-block">
        <div class="container">
          <div class="section-heading section-heading--split">
            <div>
              <p class="eyebrow">社团浏览</p>
              <h2>探索校园社团</h2>
            </div>
            <router-link to="/explore/clubs" class="link-more">查看全部 →</router-link>
          </div>
          <div class="club-grid">
            <div
              v-for="club in clubs"
              :key="club.clubId"
              class="club-card"
              @click="goClubDetail(club.clubId)"
            >
              <div class="club-card__icon">{{ club.name?.charAt(0) }}</div>
              <div class="club-card__info">
                <strong>{{ club.name }}</strong>
                <span>指导老师：{{ club.advisor || '未设置' }}</span>
                <span>成立时间：{{ club.establishedDate || '未知' }}</span>
              </div>
            </div>
          </div>
          <div v-if="clubs.length === 0" class="empty-hint">暂无社团数据</div>
        </div>
      </section>

      <!-- 活动大厅 -->
      <section id="activities-section" class="section-block section-muted">
        <div class="container">
          <div class="section-heading section-heading--split">
            <div>
              <p class="eyebrow">活动大厅</p>
              <h2>近期精彩活动</h2>
            </div>
            <router-link to="/explore/activities" class="link-more">全部活动 →</router-link>
          </div>
          <div class="activity-grid">
            <div
              v-for="act in activities"
              :key="act.activityId"
              class="activity-card"
              @click="goActivityDetail(act.activityId)"
            >
              <div class="activity-card__status" :class="statusClass(act.status)">
                {{ statusText(act.status) }}
              </div>
              <strong class="activity-card__title">{{ act.title }}</strong>
              <p class="activity-card__meta">
                <span>📍 {{ act.location || '待定' }}</span>
                <span>📅 {{ formatDate(act.eventTime) }}</span>
                <span>👥 {{ act.regCount || 0 }}/{{ act.maxParticipants || '∞' }}</span>
              </p>
              <p class="activity-card__desc">{{ act.details || '暂无描述' }}</p>
              <button
                v-if="userStore.isLoggedIn && act.status === 1"
                class="btn-signup"
                @click.stop="handleSignup(act.activityId)"
              >
                立即报名
              </button>
            </div>
          </div>
          <div v-if="activities.length === 0" class="empty-hint">暂无已发布的活动</div>
        </div>
      </section>

      <!-- 快捷服务 -->
      <section id="services-section" class="section-block">
        <div class="container">
          <div class="section-heading">
            <p class="eyebrow">快捷服务</p>
            <h2>常用功能一目了然</h2>
          </div>
          <div class="service-grid">
            <div class="service-card" @click="$router.push('/explore/clubs')">
              <span class="service-icon">🏠</span>
              <strong>社团列表</strong>
              <p>浏览全校已成立的社团，了解社团风采</p>
            </div>
            <div class="service-card" @click="$router.push('/explore/activities')">
              <span class="service-icon">🎯</span>
              <strong>活动报名</strong>
              <p>查看已发布的活动，在线报名参与</p>
            </div>
            <div class="service-card" @click="goTo('/registrations')">
              <span class="service-icon">✅</span>
              <strong>我的报名</strong>
              <p>查看报名记录和签到状态</p>
            </div>
            <div class="service-card" @click="goTo('/profile')">
              <span class="service-icon">👤</span>
              <strong>个人中心</strong>
              <p>编辑个人信息，管理账户</p>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部 -->
    <footer class="site-footer">
      <div class="container">
        <p>校园社团活动管理系统 © 2026 · 基于 Vue 3 + Spring Boot 构建</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClubList } from '@/api/club'
import { getActivityList } from '@/api/activity'
import { signup } from '@/api/registration'
import { getUnreadCount } from '@/api/message'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const quickTags = ['编程', '音乐', '运动', '学术']
const clubs = ref([])
const activities = ref([])
const unreadMsgCount = ref(0)
const stats = ref({
  clubCount: 0,
  activeActivityCount: 0,
  totalActivityCount: 0
})

onMounted(() => {
  loadClubs()
  loadActivities()
  if (userStore.isLoggedIn) {
    loadUnreadCount()
  }
})

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadMsgCount.value = res.data || 0
  } catch (e) { /* ignore */ }
}

async function loadClubs(name) {
  try {
    const res = await getClubList({ name: name || undefined })
    clubs.value = (res.data || []).slice(0, 8)
    stats.value.clubCount = res.data?.length || 0
  } catch (e) {
    clubs.value = []
  }
}

async function loadActivities() {
  try {
    const [pubRes, allRes] = await Promise.all([
      getActivityList({ status: 1 }),
      getActivityList()
    ])
    activities.value = (pubRes.data || []).slice(0, 6)
    stats.value.activeActivityCount = pubRes.data?.length || 0
    stats.value.totalActivityCount = allRes.data?.length || 0
  } catch (e) {
    activities.value = []
  }
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    loadClubs(searchKeyword.value.trim())
  } else {
    loadClubs()
  }
}

// 监听搜索框清空时恢复全部社团
watch(searchKeyword, (val) => {
  if (!val || !val.trim()) {
    loadClubs()
  }
})

function quickSearch(tag) {
  searchKeyword.value = tag
  loadClubs(tag)
}

function goClubDetail(id) {
  router.push(`/explore/clubs/${id}`)
}

function goActivityDetail(id) {
  router.push(`/explore/activities`)
}

async function handleSignup(activityId) {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录后报名')
    router.push('/login')
    return
  }
  try {
    await signup(activityId)
    ElMessage.success('报名成功！')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '报名失败')
  }
}

function goTo(path) {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }
  router.push(`/admin${path}`)
}

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
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
  return dateStr.substring(0, 10)
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7fafc 0%, #edf2f7 100%);
}

/* 顶部快捷栏 */
.top-bar {
  background: var(--color-primary, #2b6cb0);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.top-bar__inner {
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.welcome-text {
  margin: 0;
}

.top-bar__nav {
  display: flex;
  gap: 18px;
  align-items: center;
}

.top-bar__nav a {
  color: rgba(255, 255, 255, 0.9);
  transition: color 0.2s;
}

.top-bar__nav a:hover {
  color: #fff;
}

.user-greeting {
  font-weight: 600;
  color: #bee3f8;
}

.msg-link {
  position: relative;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  text-decoration: none;
}

.msg-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: #e53e3e;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.admin-link {
  color: #fbd38d !important;
  font-weight: 600;
}

/* 站点头部 */
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border, rgba(43, 108, 176, 0.1));
}

.site-header__inner {
  min-height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.site-header__brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.site-header__brand .brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--color-primary, #2b6cb0), var(--color-accent, #38a169));
  color: #fff;
}

.site-header__brand h2 {
  font-size: 20px;
  color: var(--color-primary, #2b6cb0);
  margin: 0;
}

.site-header__nav {
  display: flex;
  gap: 24px;
  color: var(--color-text-light, #718096);
  font-size: 15px;
}

.site-header__nav a:hover {
  color: var(--color-primary, #2b6cb0);
}

/* Container */
.container {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
}

/* Section */
.section-block {
  padding: 48px 0;
}

.section-muted {
  background: rgba(255, 255, 255, 0.7);
  border-top: 1px solid var(--color-border, rgba(43, 108, 176, 0.1));
  border-bottom: 1px solid var(--color-border, rgba(43, 108, 176, 0.1));
}

.section-heading {
  margin-bottom: 28px;
}

.section-heading--split {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.section-heading h2 {
  margin: 0;
  font-size: 24px;
  color: var(--color-text, #1a202c);
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--color-accent, #38a169);
  font-size: 13px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 600;
}

.link-more {
  font-size: 14px;
  color: var(--color-primary, #2b6cb0);
  font-weight: 500;
  transition: opacity 0.2s;
}

.link-more:hover {
  opacity: 0.7;
}

/* Hero Section */
.hero-section {
  padding: 48px 0 32px;
}

.hero-section__inner {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(260px, 0.8fr);
  gap: 24px;
  align-items: stretch;
}

.hero-section__content {
  padding: 40px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(235, 244, 255, 0.92));
  border: 1px solid var(--color-border);
  box-shadow: 0 16px 40px rgba(43, 108, 176, 0.08);
}

.hero-section__content h1 {
  margin: 0;
  font-size: clamp(28px, 3.5vw, 42px);
  line-height: 1.25;
  color: var(--color-primary, #2b6cb0);
}

.hero-section__desc {
  margin: 16px 0 28px;
  color: var(--color-text-light, #718096);
  font-size: 16px;
  max-width: 560px;
}

.search-panel {
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(43, 108, 176, 0.12);
}

.search-panel__label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: var(--color-primary, #2b6cb0);
  font-size: 14px;
}

.search-panel__controls {
  display: flex;
  gap: 12px;
}

.search-panel__controls input {
  flex: 1;
  min-width: 0;
  height: 46px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid rgba(43, 108, 176, 0.14);
  background: #fff;
  color: var(--color-text);
  font-size: 14px;
}

.search-panel__controls input:focus {
  outline: 2px solid rgba(43, 108, 176, 0.2);
  border-color: var(--color-primary);
}

.search-panel__controls button {
  min-width: 100px;
  height: 46px;
  padding: 0 20px;
  border-radius: 12px;
  background: var(--color-primary, #2b6cb0);
  color: #fff;
  font-weight: 500;
  transition: background 0.2s, transform 0.15s;
}

.search-panel__controls button:hover {
  background: var(--color-primary-soft, #3182ce);
  transform: scale(1.03);
}

.search-tags {
  margin-top: 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  list-style: none;
  padding: 0;
}

.search-tags li {
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(43, 108, 176, 0.08);
  color: var(--color-primary-soft, #3182ce);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, transform 0.2s;
}

.search-tags li:hover {
  background: var(--color-primary, #2b6cb0);
  color: #fff;
  transform: translateY(-1px);
}

/* Hero 右侧面板 */
.hero-section__panel {
  display: grid;
  gap: 14px;
}

.hero-card {
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(135deg, #2b6cb0 0%, #2c5282 100%);
  color: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(43, 108, 176, 0.2);
  box-shadow: 0 12px 28px rgba(43, 108, 176, 0.2);
  transition: transform 0.22s, box-shadow 0.22s;
}

.hero-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 36px rgba(43, 108, 176, 0.3);
}

.hero-card span {
  display: block;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.hero-card strong {
  font-size: 22px;
  line-height: 1.3;
}

/* 社团网格 */
.club-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.club-card {
  padding: 24px;
  border-radius: 18px;
  background: var(--color-surface, #fff);
  border: 1px solid var(--color-border);
  box-shadow: 0 4px 16px rgba(43, 108, 176, 0.06);
  cursor: pointer;
  transition: transform 0.22s, box-shadow 0.22s;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.club-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(43, 108, 176, 0.12);
}

.club-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.club-card__info {
  min-width: 0;
}

.club-card__info strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
  color: var(--color-text);
}

.club-card__info span {
  display: block;
  font-size: 12px;
  color: var(--color-text-light);
  line-height: 1.6;
}

/* 活动网格 */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.activity-card {
  padding: 24px;
  border-radius: 18px;
  background: var(--color-surface, #fff);
  border: 1px solid var(--color-border);
  box-shadow: 0 4px 16px rgba(43, 108, 176, 0.06);
  cursor: pointer;
  transition: transform 0.22s, box-shadow 0.22s;
  position: relative;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(43, 108, 176, 0.12);
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
  font-size: 16px;
  color: var(--color-text);
  margin-bottom: 8px;
}

.activity-card__meta {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-light);
  display: flex;
  gap: 14px;
}

.activity-card__desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.btn-signup {
  margin-top: 14px;
  padding: 8px 20px;
  border-radius: 10px;
  background: var(--color-accent, #38a169);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s, transform 0.15s;
}

.btn-signup:hover {
  background: var(--color-accent-soft, #48bb78);
  transform: scale(1.03);
}

/* 服务网格 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.service-card {
  padding: 28px 24px;
  border-radius: 18px;
  background: var(--color-surface, #fff);
  border: 1px solid var(--color-border);
  box-shadow: 0 4px 16px rgba(43, 108, 176, 0.06);
  cursor: pointer;
  transition: transform 0.22s, box-shadow 0.22s;
  text-align: center;
}

.service-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(43, 108, 176, 0.12);
}

.service-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}

.service-card strong {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
  color: var(--color-primary);
}

.service-card p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
}

/* 空状态 */
.empty-hint {
  text-align: center;
  padding: 40px;
  color: var(--color-text-light);
  font-size: 14px;
}

/* 底部 */
.site-footer {
  padding: 24px 0;
  text-align: center;
  color: var(--color-text-light);
  font-size: 13px;
  border-top: 1px solid var(--color-border);
}

/* 响应式 */
@media (max-width: 960px) {
  .hero-section__inner {
    grid-template-columns: 1fr;
  }

  .club-grid,
  .service-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .activity-grid {
    grid-template-columns: 1fr;
  }

  .site-header__inner,
  .top-bar__inner {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    padding: 12px 0;
  }

  .section-heading--split {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .container {
    width: min(100% - 32px, 1120px);
  }

  .club-grid,
  .service-grid {
    grid-template-columns: 1fr;
  }

  .hero-section__content {
    padding: 24px;
  }

  .search-panel__controls {
    flex-direction: column;
  }
}
</style>
