<template>
  <div class="explore-page">
    <header class="explore-header">
      <div class="container explore-header__inner">
        <router-link to="/explore/clubs" class="back-btn">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          返回社团列表
        </router-link>
        <h1>{{ club.name || '社团详情' }}</h1>
      </div>
    </header>

    <main class="container explore-main" v-loading="loading">
      <!-- 社团基本信息 -->
      <div class="info-card">
        <div class="info-card__header">
          <div class="club-icon">{{ club.name?.charAt(0) || '?' }}</div>
          <div class="club-basic">
            <h2>{{ club.name }}</h2>
            <div class="club-tags">
              <span class="tag-category">{{ club.category || '未分类' }}</span>
              <span class="tag-date">成立于 {{ club.establishedDate || '未知' }}</span>
            </div>
          </div>
          <div class="club-action">
            <el-button v-if="!isLoggedIn" type="primary" @click="$router.push('/login')">登录后加入</el-button>
            <el-button v-else-if="myMembership === null" type="primary" @click="handleApply">申请加入</el-button>
            <el-button v-else-if="myMembership === 0" type="info" disabled>申请中...</el-button>
            <el-button v-else-if="myMembership === 1" type="danger" plain @click="handleQuit">退出社团</el-button>
          </div>
        </div>
        <div class="info-card__body">
          <div class="info-row">
            <span class="info-label">指导老师</span>
            <span>{{ club.advisor || '未指定' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">社团简介</span>
            <span>{{ club.description || '暂无简介' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">成员人数</span>
            <span>{{ members.length }} 人</span>
          </div>
        </div>
      </div>

      <!-- 成员列表 -->
      <div class="section-card">
        <h3 class="section-card__title">社团成员</h3>
        <div class="member-grid">
          <div v-for="m in members" :key="m.id" class="member-item">
            <div class="member-avatar">{{ m.studentName?.charAt(0) || m.studentId?.charAt(0) || '?' }}</div>
            <div class="member-info">
              <span class="member-name">{{ m.studentName || m.studentId }}</span>
              <span class="member-position">{{ m.position }}</span>
            </div>
          </div>
        </div>
        <div v-if="members.length === 0" class="empty-text">暂无成员</div>
      </div>

      <!-- 社团活动 -->
      <div class="section-card">
        <h3 class="section-card__title">社团活动</h3>
        <div class="activity-grid">
          <div v-for="a in activities" :key="a.activityId" class="activity-card">
            <div class="activity-card__header">
              <span class="activity-card__title">{{ a.title }}</span>
              <span class="activity-card__status" :class="statusClass(a.status)">{{ statusText(a.status) }}</span>
            </div>
            <p class="activity-card__details">{{ a.details || '暂无活动详情' }}</p>
            <div class="activity-card__meta">
              <span>📍 {{ a.location || '待定' }}</span>
              <span>📅 {{ formatTime(a.eventTime) }}</span>
              <span>👥 {{ a.regCount ?? 0 }}/{{ a.maxParticipants ?? '不限' }}</span>
            </div>
            <div class="activity-card__footer" v-if="a.status === 1">
              <el-button v-if="!isLoggedIn" size="small" @click="$router.push('/login')">登录后报名</el-button>
              <el-button v-else-if="myRegisteredIds.has(a.activityId)" size="small" type="success" plain disabled>已报名</el-button>
              <el-tooltip v-else-if="myMembership !== 1" content="仅社团成员可报名，请先加入社团" placement="top">
                <span>
                  <el-button size="small" disabled>报名</el-button>
                </span>
              </el-tooltip>
              <el-button v-else size="small" type="primary" :loading="signupLoading === a.activityId" @click="handleSignup(a)">报名</el-button>
            </div>
          </div>
        </div>
        <div v-if="activities.length === 0" class="empty-text">暂无活动</div>
      </div>

      <!-- 成员评价 -->
      <div class="section-card">
        <h3 class="section-card__title">成员评价</h3>

        <!-- 发表评价 -->
        <div v-if="isLoggedIn && myMembership === 1" class="comment-form">
          <div class="rating-row">
            <span>评分：</span>
            <div class="star-rating">
              <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= commentForm.rating }" @click="commentForm.rating = s">★</span>
            </div>
          </div>
          <el-input v-model="commentForm.content" type="textarea" :rows="2" placeholder="写下你对这个社团的评价..." />
          <el-button type="primary" size="small" :loading="commentSubmitting" @click="submitComment" style="margin-top:10px">发表评价</el-button>
        </div>
        <div v-else-if="isLoggedIn && myMembership !== 1" class="comment-hint">
          加入社团后才能发表评价
        </div>

        <!-- 评价列表 -->
        <div class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-item__header">
              <div class="comment-avatar">{{ c.studentName?.charAt(0) || '?' }}</div>
              <div class="comment-meta">
                <span class="comment-name">{{ c.studentName || c.studentId }}</span>
                <span class="comment-time">{{ formatTime(c.createTime) }}</span>
              </div>
              <div class="comment-stars">
                <span v-for="s in 5" :key="s" class="star-sm" :class="{ active: s <= c.rating }">★</span>
              </div>
            </div>
            <p class="comment-content">{{ c.content }}</p>
          </div>
        </div>
        <div v-if="comments.length === 0" class="empty-text">暂无评价，快来成为第一个评价者吧</div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClubPublicDetail } from '@/api/club'
import { getActivityList } from '@/api/activity'
import { getMemberList, applyJoin, quitClub } from '@/api/clubMember'
import { getClubComments, addClubComment } from '@/api/clubComment'
import { signup, getMyRegistrations } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const club = ref({})
const members = ref([])
const comments = ref([])
const activities = ref([])
const loading = ref(false)
const commentSubmitting = ref(false)
const myMembership = ref(null) // null=未加入, 0=待审批, 1=已加入
const myRegisteredIds = ref(new Set()) // 我已报名的活动id
const signupLoading = ref(null) // 正在报名的活动id

const isLoggedIn = computed(() => userStore.isLoggedIn)

const commentForm = reactive({ content: '', rating: 5 })

onMounted(async () => {
  const clubId = route.params.id
  loading.value = true
  try {
    await Promise.all([loadClub(clubId), loadMembers(clubId), loadComments(clubId), loadActivities(clubId), loadMyRegistrations()])
    checkMembership()
  } finally {
    loading.value = false
  }
})

async function loadClub(clubId) {
  try {
    const res = await getClubPublicDetail(clubId)
    club.value = res.data || {}
  } catch (e) {
    club.value = {}
  }
}

async function loadMembers(clubId) {
  try {
    const res = await getMemberList(clubId, { status: 1 })
    const list = res.data || []
    // 补充姓名（用studentId的前缀做fallback）
    members.value = list
  } catch (e) {
    members.value = []
  }
}

async function loadComments(clubId) {
  try {
    const res = await getClubComments(clubId)
    comments.value = res.data || []
  } catch (e) {
    comments.value = []
  }
}

async function loadActivities(clubId) {
  try {
    const res = await getActivityList({ clubId })
    // 只展示已发布和已结束的活动
    activities.value = (res.data || []).filter(a => a.status === 1 || a.status === 2)
  } catch (e) {
    activities.value = []
  }
}

async function loadMyRegistrations() {
  if (!userStore.isLoggedIn) {
    myRegisteredIds.value = new Set()
    return
  }
  try {
    const res = await getMyRegistrations()
    myRegisteredIds.value = new Set((res.data || []).map(r => r.activityId))
  } catch (e) {
    myRegisteredIds.value = new Set()
  }
}

async function handleSignup(activity) {
  signupLoading.value = activity.activityId
  try {
    await signup(activity.activityId)
    ElMessage.success('报名成功')
    myRegisteredIds.value = new Set([...myRegisteredIds.value, activity.activityId])
    activity.regCount = (activity.regCount ?? 0) + 1
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '报名失败')
  } finally {
    signupLoading.value = null
  }
}

function statusText(s) {
  return { 0: '待审批', 1: '报名中', 2: '已结束', 3: '已驳回' }[s] || '未知'
}

function statusClass(s) {
  return { 0: 'pending', 1: 'active', 2: 'finished', 3: 'finished' }[s] || ''
}

function checkMembership() {
  if (!userStore.isLoggedIn) {
    myMembership.value = null
    return
  }
  const studentId = userStore.userInfo?.studentId
  const me = members.value.find(m => m.studentId === studentId)
  if (me) {
    myMembership.value = 1
  } else {
    // 也需要检查是否有待审批的申请，但这里简化处理
    myMembership.value = null
  }
}

async function handleApply() {
  try {
    await applyJoin({ clubId: club.value.clubId })
    ElMessage.success('申请已提交，等待审批')
    myMembership.value = 0
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '申请失败')
  }
}

async function handleQuit() {
  await ElMessageBox.confirm('确定要退出该社团吗？', '提示', { type: 'warning' })
  try {
    await quitClub(club.value.clubId)
    ElMessage.success('已退出社团')
    myMembership.value = null
    loadMembers(club.value.clubId)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '退出失败')
  }
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  commentSubmitting.value = true
  try {
    await addClubComment({ clubId: club.value.clubId, content: commentForm.content, rating: commentForm.rating })
    ElMessage.success('评价成功')
    commentForm.content = ''
    commentForm.rating = 5
    loadComments(club.value.clubId)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '评价失败')
  } finally {
    commentSubmitting.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
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
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.container {
  width: min(900px, calc(100% - 48px));
  margin: 0 auto;
}

.explore-main {
  padding: 32px 0 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.info-card__header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 28px;
  border-bottom: 1px solid var(--color-border);
}

.club-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-primary, #2b6cb0), var(--color-accent, #38a169));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
}

.club-basic {
  flex: 1;
}

.club-basic h2 {
  margin: 0 0 6px;
  font-size: 20px;
  color: var(--color-text);
}

.club-tags {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.tag-category {
  padding: 2px 10px;
  border-radius: 6px;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
}

.tag-date {
  color: var(--color-text-light);
}

.info-card__body {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.info-label {
  min-width: 70px;
  color: var(--color-text-light);
  font-weight: 500;
}

/* 段落卡片 */
.section-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  padding: 28px;
}

.section-card__title {
  margin: 0 0 20px;
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
}

/* 成员网格 */
.member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
}

.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.member-info {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.member-position {
  font-size: 12px;
  color: var(--color-text-light);
}

/* 活动卡片 */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.activity-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border-radius: 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  transition: box-shadow 0.2s, transform 0.2s;
}

.activity-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.activity-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.activity-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.activity-card__status {
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.activity-card__status.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.activity-card__status.active {
  background: rgba(56, 161, 105, 0.1);
  color: var(--color-accent, #38a169);
}

.activity-card__status.finished {
  background: rgba(113, 128, 150, 0.1);
  color: var(--color-text-light);
}

.activity-card__details {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  font-size: 12px;
  color: var(--color-text-light);
}

.activity-card__footer {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed var(--color-border);
  display: flex;
  justify-content: flex-end;
}

/* 评价 */
.comment-form {
  padding: 16px;
  border-radius: 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  margin-bottom: 20px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  color: var(--color-text);
}

.star-rating {
  display: flex;
  gap: 4px;
}

.star {
  font-size: 22px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.15s;
}

.star.active {
  color: #f59e0b;
}

.comment-hint {
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(43, 108, 176, 0.04);
  color: var(--color-text-light);
  font-size: 13px;
  margin-bottom: 20px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 16px;
  border-radius: 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
}

.comment-item__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.comment-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.comment-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.comment-time {
  font-size: 12px;
  color: var(--color-text-light);
}

.comment-stars {
  display: flex;
  gap: 2px;
}

.star-sm {
  font-size: 14px;
  color: #ddd;
}

.star-sm.active {
  color: #f59e0b;
}

.comment-content {
  margin: 0;
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.6;
}

.empty-text {
  text-align: center;
  padding: 24px;
  color: var(--color-text-light);
  font-size: 14px;
}

@media (max-width: 640px) {
  .info-card__header {
    flex-direction: column;
    align-items: flex-start;
  }
  .member-grid {
    grid-template-columns: 1fr;
  }
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
