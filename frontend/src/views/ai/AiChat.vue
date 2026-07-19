<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="chat-header__left">
        <h3>AI 助手</h3>
        <p>基于社团数据的智能问答</p>
      </div>
      <div class="chat-header__right">
        <el-select v-model="clubId" placeholder="全局对话" clearable size="small" style="width: 180px">
          <el-option :value="null" label="🌐 全局（不绑定社团）" />
          <el-option v-for="c in clubs" :key="c.clubId" :label="c.name" :value="c.clubId" />
        </el-select>
        <el-button size="small" text @click="clearHistory">清空对话</el-button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="chat-empty">
        <div class="chat-empty__icon">🤖</div>
        <h4>你好，我是社团运营 AI 助手</h4>
        <p>你可以询问社团管理、活动策划、成员运营等方面的问题。</p>
        <div class="chat-empty__hints">
          <button v-for="hint in hints" :key="hint" @click="sendHint(hint)">{{ hint }}</button>
        </div>
      </div>

      <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-msg', `chat-msg--${msg.role}`]">
        <div class="chat-msg__avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="chat-msg__bubble">
          <div v-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMd(msg.content)"></div>
          <div v-else class="chat-msg__text">{{ msg.content }}</div>
        </div>
      </div>

      <!-- 思考中：跳动圆点 -->
      <div v-if="streaming && !streamingContent" class="chat-msg chat-msg--assistant">
        <div class="chat-msg__avatar">AI</div>
        <div class="chat-msg__bubble">
          <div class="thinking-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 流式回复中：打字机效果 -->
      <div v-if="streaming && streamingContent" class="chat-msg chat-msg--assistant">
        <div class="chat-msg__avatar">AI</div>
        <div class="chat-msg__bubble">
          <div class="markdown-body typewriter" v-html="renderMd(streamingContent)"></div>
          <span class="typing-cursor">▌</span>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入你的问题... (Ctrl+Enter 发送)"
        :disabled="streaming"
        @keydown.ctrl.enter="sendMessage"
        resize="none"
      />
      <el-button type="primary" :disabled="!inputText.trim() || streaming" @click="sendMessage" class="send-btn">
        {{ streaming ? '回复中...' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { chatStream } from '@/api/aiReport'
import { getClubList } from '@/api/club'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const STORAGE_KEY = 'ai_chat_messages'
const CLUB_STORAGE_KEY = 'ai_chat_clubId'

function loadMessages() {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : []
  } catch { return [] }
}

function saveMessages() {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value))
}

function loadClubId() {
  try {
    const saved = sessionStorage.getItem(CLUB_STORAGE_KEY)
    return saved ? JSON.parse(saved) : null
  } catch { return null }
}

function saveClubId() {
  sessionStorage.setItem(CLUB_STORAGE_KEY, JSON.stringify(clubId.value))
}

const clubs = ref([])
const clubId = ref(loadClubId())
const messages = ref(loadMessages())
const inputText = ref('')
const streaming = ref(false)
const streamingContent = ref('')
const messagesRef = ref(null)
let streamController = null

watch(messages, saveMessages, { deep: true })
watch(clubId, saveClubId)

const hints = [
  '如何提高社团活动的参与率？',
  '给社团招新活动提一些建议',
  '如何评估社团的运营质量？',
  '活动签到率低怎么改善？'
]

function renderMd(text) {
  if (!text) return ''
  return marked(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function sendHint(text) {
  inputText.value = text
  sendMessage()
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  streaming.value = true
  streamingContent.value = ''

  const recentMessages = messages.value.slice(-20).map(m => ({
    role: m.role,
    content: m.content
  }))

  streamController = chatStream({
    messages: recentMessages,
    clubId: clubId.value,
    onMessage(token) {
      streamingContent.value += token
      scrollToBottom()
    },
    onDone() {
      messages.value.push({ role: 'assistant', content: streamingContent.value })
      streamingContent.value = ''
      streaming.value = false
      scrollToBottom()
    },
    onError(err) {
      if (streamingContent.value) {
        messages.value.push({ role: 'assistant', content: streamingContent.value })
      } else {
        messages.value.push({ role: 'assistant', content: '抱歉，请求出现错误：' + (err.message || '网络异常') })
      }
      streamingContent.value = ''
      streaming.value = false
      scrollToBottom()
    }
  })
}

function clearHistory() {
  if (streaming.value) {
    ElMessage.warning('请等待当前回复完成')
    return
  }
  messages.value = []
  sessionStorage.removeItem(STORAGE_KEY)
}

onMounted(async () => {
  try {
    const res = await getClubList()
    clubs.value = res.data || []
  } catch (e) { /* ignore */ }
  if (messages.value.length) scrollToBottom()
})

onBeforeUnmount(() => {
  if (streamController) streamController.abort()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 16px;
}

.chat-header__left h3 {
  margin: 0 0 2px;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

.chat-header__left p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
}

.chat-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: var(--color-text-light);
}

.chat-empty__icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.chat-empty h4 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--color-text);
}

.chat-empty p {
  margin: 0 0 24px;
  font-size: 14px;
}

.chat-empty__hints {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 500px;
}

.chat-empty__hints button {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.chat-empty__hints button:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-lighter);
}

.chat-msg {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.chat-msg--user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-msg--assistant {
  align-self: flex-start;
}

.chat-msg__avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-msg--user .chat-msg__avatar {
  background: var(--color-primary-lighter);
  color: var(--color-primary);
}

.chat-msg--assistant .chat-msg__avatar {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(103, 194, 58, 0.15));
  color: var(--color-primary);
}

.chat-msg__bubble {
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.7;
  font-size: 14px;
}

.chat-msg--user .chat-msg__bubble {
  background: var(--color-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-msg--assistant .chat-msg__bubble {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: 4px;
  color: var(--color-text);
}

.chat-msg__text {
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.5;
}

.send-btn {
  border-radius: 12px;
  height: 44px;
  padding: 0 24px;
}

/* markdown in chat */
.chat-msg--assistant .markdown-body :deep(h1),
.chat-msg--assistant .markdown-body :deep(h2),
.chat-msg--assistant .markdown-body :deep(h3),
.chat-msg--assistant .markdown-body :deep(h4) {
  margin-top: 12px;
  margin-bottom: 6px;
  font-weight: 600;
  color: var(--color-primary);
}

.chat-msg--assistant .markdown-body :deep(h1) { font-size: 17px; }
.chat-msg--assistant .markdown-body :deep(h2) { font-size: 16px; }
.chat-msg--assistant .markdown-body :deep(h3) { font-size: 15px; }

.chat-msg--assistant .markdown-body :deep(p) {
  margin-bottom: 8px;
}

.chat-msg--assistant .markdown-body :deep(ul),
.chat-msg--assistant .markdown-body :deep(ol) {
  padding-left: 18px;
  margin-bottom: 8px;
}

.chat-msg--assistant .markdown-body :deep(li) {
  margin-bottom: 3px;
}

.chat-msg--assistant .markdown-body :deep(code) {
  background: rgba(0,0,0,0.04);
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 13px;
}

.chat-msg--assistant .markdown-body :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  padding-left: 10px;
  color: var(--color-text-light);
  margin: 8px 0;
}

/* 思考中跳动圆点 */
.thinking-dots {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.4;
  animation: dotBounce 1.4s infinite ease-in-out both;
}

.thinking-dots span:nth-child(1) { animation-delay: 0s; }
.thinking-dots span:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 打字机光标 */
.typing-cursor {
  display: inline-block;
  color: var(--color-primary);
  animation: cursorBlink 1s step-end infinite;
  margin-left: 2px;
  font-weight: 300;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.typewriter :deep(*:last-child) {
  display: inline;
}
</style>
