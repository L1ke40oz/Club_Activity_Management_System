<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">站内信</h2>
        <p class="page-desc">查看系统通知和消息</p>
      </div>
      <el-button v-if="messages.length > 0" type="primary" plain size="small" @click="handleReadAll">全部已读</el-button>
    </div>

    <div class="message-list">
      <div v-for="msg in messages" :key="msg.id" class="message-card" :class="{ unread: msg.isRead === 0 }" @click="handleRead(msg)">
        <div class="message-card__dot" v-if="msg.isRead === 0"></div>
        <div class="message-card__body">
          <h4>{{ msg.title }}</h4>
          <p>{{ msg.content }}</p>
          <span class="message-time">{{ formatTime(msg.createTime) }}</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && messages.length === 0" description="暂无消息" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyMessages, markRead, markAllRead } from '@/api/message'
import { ElMessage } from 'element-plus'

const messages = ref([])
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadMessages() {
  loading.value = true
  try {
    const res = await getMyMessages()
    messages.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleRead(msg) {
  if (msg.isRead === 0) {
    await markRead(msg.id)
    msg.isRead = 1
  }
}

async function handleReadAll() {
  await markAllRead()
  messages.value.forEach(m => m.isRead = 1)
  ElMessage.success('全部标记为已读')
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.page-container {
  animation: fadeIn 0.3s ease;
  max-width: 700px;
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

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.message-card:hover {
  box-shadow: 0 4px 16px rgba(43, 108, 176, 0.08);
}

.message-card.unread {
  background: rgba(43, 108, 176, 0.02);
  border-color: rgba(43, 108, 176, 0.15);
}

.message-card__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
  margin-top: 8px;
}

.message-card__body {
  flex: 1;
}

.message-card__body h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--color-text);
}

.message-card__body p {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: var(--color-text-light);
  opacity: 0.7;
}
</style>
