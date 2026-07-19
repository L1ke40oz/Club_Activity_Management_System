<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ club.name || '社团详情' }}</h2>
        <p class="page-desc">查看社团信息和成员</p>
      </div>
      <el-button plain @click="$router.push('/admin/my-clubs')">← 返回我的社团</el-button>
    </div>

    <!-- 基本信息 -->
    <div class="info-card">
      <div class="info-card__header">
        <div class="club-icon">{{ club.name?.charAt(0) || '?' }}</div>
        <div class="club-basic">
          <h3>{{ club.name }}</h3>
          <el-tag size="small" effect="plain">{{ club.category || '未分类' }}</el-tag>
        </div>
      </div>
      <el-descriptions :column="2" border class="info-desc">
        <el-descriptions-item label="指导老师">{{ club.advisor || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="成立日期">{{ club.establishedDate || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="成员人数">{{ members.length }} 人</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ club.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ club.description || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 成员列表 -->
    <div class="members-card">
      <h3>社团成员</h3>
      <el-table :data="members" stripe style="width: 100%">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="position" label="职务" width="100" />
        <el-table-column label="加入时间">
          <template #default="{ row }">{{ formatTime(row.joinTime) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 社团活动 -->
    <div class="members-card">
      <h3>社团活动</h3>
      <el-table :data="activities" stripe style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="160" />
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column label="活动时间" width="160">
          <template #default="{ row }">{{ formatTime(row.eventTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="actStatusType(row.status)" size="small" effect="light">{{ actStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="activities.length === 0" description="暂无活动" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getClubDetail } from '@/api/club'
import { getMemberList } from '@/api/clubMember'
import { getActivityList } from '@/api/activity'

const route = useRoute()
const club = ref({})
const members = ref([])
const activities = ref([])

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function actStatusText(s) {
  return { 0: '待审批', 1: '已发布', 2: '已结束', 3: '已驳回' }[s] || '未知'
}
function actStatusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info'
}

onMounted(async () => {
  const clubId = route.params.id
  try {
    const [clubRes, memberRes, actRes] = await Promise.all([
      getClubDetail(clubId),
      getMemberList(clubId, { status: 1 }),
      getActivityList({ clubId })
    ])
    club.value = clubRes.data || {}
    members.value = memberRes.data || []
    activities.value = actRes.data || []
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container {
  max-width: 900px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
}

.info-card {
  background: #fff;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  padding: 24px;
  margin-bottom: 20px;
}

.info-card__header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.club-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}

.club-basic h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--color-text);
}

.members-card {
  background: #fff;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  padding: 24px;
}

.members-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: var(--color-text);
}
</style>
