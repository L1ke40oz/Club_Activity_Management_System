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
        <h1>全部社团</h1>
        <div class="explore-header__actions">
          <input v-model="searchName" type="text" placeholder="搜索社团..." @input="handleSearch" />
        </div>
      </div>
    </header>

    <main class="container explore-main">
      <div class="club-grid">
        <div v-for="club in filteredClubs" :key="club.clubId" class="club-card" @click="viewDetail(club)">
          <div class="club-card__icon">{{ club.name?.charAt(0) }}</div>
          <div class="club-card__info">
            <strong>{{ club.name }}</strong>
            <span class="club-card__category">{{ club.category || '未分类' }}</span>
            <p class="club-card__desc">{{ club.description || '暂无简介' }}</p>
            <div class="club-card__meta">
              <span>👨‍🏫 {{ club.advisor || '未指定' }}</span>
              <span>📅 {{ club.establishedDate || '未知' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredClubs.length === 0 && !loading" class="empty-hint">
        {{ searchName ? '没有找到匹配的社团' : '暂无社团数据' }}
      </div>
    </main>

    <!-- 详情弹窗已移除，改为跳转详情页 -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getClubList } from '@/api/club'

const router = useRouter()
const clubs = ref([])
const searchName = ref('')
const loading = ref(false)

const filteredClubs = computed(() => {
  if (!searchName.value.trim()) return clubs.value
  const keyword = searchName.value.trim().toLowerCase()
  return clubs.value.filter(c =>
    c.name?.toLowerCase().includes(keyword) ||
    c.category?.toLowerCase().includes(keyword)
  )
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getClubList()
    clubs.value = res.data || []
  } finally {
    loading.value = false
  }
})

function handleSearch() {
  // filtering is done by computed
}

function viewDetail(club) {
  router.push(`/explore/clubs/${club.clubId}`)
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

.explore-header__actions input {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  font-size: 14px;
  width: 200px;
  color: var(--color-text);
}

.explore-header__actions input:focus {
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

.club-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

.club-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.club-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(43, 108, 176, 0.1);
}

.club-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary, #2b6cb0), var(--color-accent, #38a169));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.club-card__info {
  min-width: 0;
}

.club-card__info strong {
  display: block;
  font-size: 16px;
  color: var(--color-text);
  margin-bottom: 4px;
}

.club-card__category {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--color-primary-lighter);
  color: var(--color-primary);
  font-size: 12px;
  margin-bottom: 8px;
}

.club-card__desc {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.club-card__meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--color-text-light);
}

.empty-hint {
  text-align: center;
  padding: 60px;
  color: var(--color-text-light);
  font-size: 15px;
}

@media (max-width: 768px) {
  .club-grid {
    grid-template-columns: 1fr;
  }
  .explore-header__inner {
    flex-wrap: wrap;
    padding: 12px 0;
  }
}
</style>
