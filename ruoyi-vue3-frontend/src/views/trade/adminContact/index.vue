<template>
  <div class="app-container admin-contact">
    <el-row :gutter="16" class="contact-container">
      <!-- 左侧：用户会话列表 -->
      <el-col :span="8" class="left-panel">
        <div class="panel-header">
          <span class="panel-title">用户咨询</span>
          <el-button text :icon="'Refresh'" @click="loadConversations" />
        </div>
        <div class="conversation-list" v-loading="convLoading">
          <div
            v-for="conv in conversations"
            :key="conv.peerId"
            class="conv-item"
            :class="{ active: activeUserId === conv.peerId }"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar">
              <el-avatar :size="40" :src="resolveAvatar(conv.peerAvatar)">
                {{ conv.peerNickname?.charAt(0) || '用' }}
              </el-avatar>
              <span v-if="conv.unreadCount" class="unread-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
            </div>
            <div class="conv-body">
              <div class="conv-top">
                <span class="conv-name">{{ conv.peerNickname }}</span>
                <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-preview">{{ conv.lastContent || '暂无消息' }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!convLoading && conversations.length === 0" description="暂无用户咨询" />
        </div>
      </el-col>

      <!-- 右侧：聊天区域 -->
      <el-col :span="16" class="right-panel">
        <template v-if="activeUserId">
          <div class="chat-header">
            <el-avatar :size="36" :src="resolveAvatar(activeUser?.peerAvatar)">
              {{ activeUser?.peerNickname?.charAt(0) || '用' }}
            </el-avatar>
            <span class="chat-user-name">{{ activeUser?.peerNickname }}</span>
          </div>
          <div class="chat-messages" ref="msgContainer" v-loading="msgLoading">
            <div
              v-for="msg in messages"
              :key="msg.messageId"
              class="msg-row"
              :class="{ 'is-mine': msg.mine }"
            >
              <div class="msg-bubble" :class="{ mine: msg.mine }">
                <div v-if="isImageMsg(msg.content)" class="msg-image">
                  <el-image
                    :src="resolveImage(msg.content)"
                    :preview-src-list="[resolveImage(msg.content)]"
                    preview-teleported
                    fit="cover"
                    style="width:160px;height:160px;border-radius:8px;"
                  />
                </div>
                <div v-else class="msg-text">{{ msg.content }}</div>
              </div>
              <div class="msg-time" :class="{ mine: msg.mine }">
                {{ formatTime(msg.createTime) }}
                <span v-if="msg.mine" class="msg-status">{{ msg.readStatus === '1' ? '已读' : '未读' }}</span>
              </div>
            </div>
            <el-empty v-if="!msgLoading && messages.length === 0" description="暂无消息记录" :image-size="60" />
          </div>
          <div class="chat-input">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入回复内容..."
              resize="none"
              @keyup.enter.ctrl="sendReply"
            />
            <el-button type="primary" :disabled="!inputText.trim()" @click="sendReply" :loading="sending">
              发送
            </el-button>
          </div>
        </template>
        <div v-else class="chat-placeholder">
          <el-empty description="请选择左侧用户开始回复" :image-size="80" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="AdminContact">
import { listAdminConversations, listAdminMessages, adminReply, adminMarkRead, getAdminInfo } from '@/api/trade/message'
import { isExternal } from '@/utils/validate'

const { proxy } = getCurrentInstance()

const conversations = ref([])
const messages = ref([])
const convLoading = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const activeUserId = ref(null)
const activeUser = ref(null)
const adminId = ref(null)
const inputText = ref('')
const msgContainer = ref(null)

// 轮询定时器
let pollTimer = null

onMounted(async () => {
  try {
    const res = await getAdminInfo()
    adminId.value = res.data.adminId
  } catch (_) {
    proxy.$modal.msgError('获取管理员信息失败')
  }
  loadConversations()
  pollTimer = setInterval(loadConversations, 15000)
})

onUnmounted(() => {
  clearInterval(pollTimer)
})

async function loadConversations() {
  convLoading.value = true
  try {
    const res = await listAdminConversations({ pageNum: 1, pageSize: 50 })
    conversations.value = res.rows || []

    // 若当前选中用户仍在列表中，刷新消息
    if (activeUserId.value) {
      const found = conversations.value.find(c => c.peerId === activeUserId.value)
      if (found) activeUser.value = found
      loadMessages()
    }
  } catch (_) {
    // ignore
  } finally {
    convLoading.value = false
  }
}

async function loadMessages() {
  if (!activeUserId.value || !adminId.value) return
  msgLoading.value = true
  try {
    const res = await listAdminMessages({
      userId: activeUserId.value,
      adminId: adminId.value,
      pageNum: 1,
      pageSize: 200
    })
    messages.value = (res.rows || []).reverse()
    scrollToBottom()
  } catch (_) {
    // ignore
  } finally {
    msgLoading.value = false
  }
}

async function selectConversation(conv) {
  activeUserId.value = conv.peerId
  activeUser.value = conv
  conv.unreadCount = 0
  loadMessages()
  // 异步标记已读
  try { await adminMarkRead(conv.peerId) } catch (_) {}
}

async function sendReply() {
  const content = inputText.value.trim()
  if (!content || !activeUserId.value) return

  sending.value = true
  try {
    await adminReply({
      receiverId: activeUserId.value,
      content: content
    })
    inputText.value = ''
    // 本地即时追加消息
    messages.value.push({
      messageId: Date.now(),
      senderId: adminId.value,
      receiverId: activeUserId.value,
      content: content,
      readStatus: '0',
      createTime: new Date().toISOString(),
      mine: true
    })
    scrollToBottom()
    loadConversations()
  } catch (_) {
    proxy.$modal.msgError('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

function isImageMsg(content) {
  if (!content) return false
  if (content.startsWith('/profile/') && content.includes('.')) return true
  if ((content.startsWith('http://') || content.startsWith('https://')) &&
      /\.(jpg|jpeg|png|gif|webp|bmp)(\?|$)/i.test(content)) return true
  return false
}

function resolveImage(url) {
  if (!url) return ''
  if (url.startsWith('/profile/')) {
    return (import.meta.env.VITE_APP_BASE_API || '') + url
  }
  if (isExternal(url)) return url
  return (import.meta.env.VITE_APP_BASE_API || '') + url
}

function resolveAvatar(url) {
  if (!url) return ''
  if (isExternal(url)) return url
  return (import.meta.env.VITE_APP_BASE_API || '') + url
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const dt = new Date(timeStr)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const msgDay = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate())
    const hm = `${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`

    if (msgDay.getTime() === today.getTime()) return hm
    const yesterday = new Date(today.getTime() - 86400000)
    if (msgDay.getTime() === yesterday.getTime()) return `昨天 ${hm}`
    if (dt.getFullYear() === now.getFullYear()) {
      return `${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')} ${hm}`
    }
    return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`
  } catch (_) {
    return timeStr
  }
}
</script>

<style scoped>
.admin-contact {
  padding: 16px;
}

.contact-container {
  height: calc(100vh - 140px);
  min-height: 500px;
}

/* ===== 左侧面板 ===== */
.left-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background .2s;
  border-bottom: 1px solid #f5f5f5;
}

.conv-item:hover {
  background: #f8fafc;
}

.conv-item.active {
  background: #eef2ff;
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.conv-body {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-size: 11px;
  color: #94a3b8;
  flex-shrink: 0;
}

.conv-preview {
  font-size: 12px;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

/* ===== 右侧面板 ===== */
.right-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.chat-user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
}

.msg-row {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.msg-row.is-mine {
  align-items: flex-end;
}

.msg-bubble {
  max-width: 65%;
  padding: 10px 14px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,.04);
}

.msg-bubble.mine {
  background: #4F6EF7;
  color: #fff;
  border-radius: 12px;
  border-top-right-radius: 4px;
}

.msg-image {
  line-height: 0;
}

.msg-text {
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
}

.msg-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.msg-time.mine {
  text-align: right;
}

.msg-status {
  margin-left: 6px;
  font-size: 10px;
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  align-items: flex-end;
  flex-shrink: 0;
}

.chat-input :deep(.el-textarea__inner) {
  resize: none;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}
</style>
