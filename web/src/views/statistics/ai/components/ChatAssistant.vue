<template>
  <div class="chat-assistant">
    <el-row :gutter="20">
      <!-- 左侧：对话区 -->
      <el-col :span="16">
        <div class="chat-container">
          <div class="chat-header">
            <span class="title">智能数据助手</span>
            <el-button type="danger" link size="small" @click="clearHistory">
              清除历史
            </el-button>
          </div>
          <div class="chat-messages" ref="messagesContainer">
            <div
                v-for="(msg, index) in messages"
                :key="index"
                :class="['message', msg.role]"
            >
              <div class="message-avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><Cpu /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>
            <div v-if="loading" class="message assistant">
              <div class="message-avatar">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
          <div class="chat-input-area">
            <div class="suggest-questions">
              <el-tag
                  v-for="q in suggestQuestions"
                  :key="q"
                  size="small"
                  @click="sendMessage(q)"
                  class="suggest-tag"
              >
                {{ q }}
              </el-tag>
            </div>
            <div class="input-wrapper">
              <el-input
                  v-model="inputMessage"
                  type="textarea"
                  :rows="2"
                  placeholder="输入您的问题，例如：最近三年人口增长趋势如何？"
                  @keydown.enter.prevent="sendMessage"
              />
              <el-button type="primary" :loading="loading" @click="sendMessage">
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：数据概览 -->
      <el-col :span="8">
        <div class="stats-card">
          <div class="stats-header">
            <span class="title">今日数据概览</span>
          </div>
          <div class="stats-list">
            <div class="stats-item">
              <span class="label">总人口</span>
              <span class="value">1,250万</span>
            </div>
            <div class="stats-item">
              <span class="label">医疗机构</span>
              <span class="value">3,850家</span>
            </div>
            <div class="stats-item">
              <span class="label">卫生人员</span>
              <span class="value">12.5万人</span>
            </div>
            <div class="stats-item">
              <span class="label">床位数</span>
              <span class="value">8.5万张</span>
            </div>
            <div class="stats-item">
              <span class="label">门诊量</span>
              <span class="value">3,520万人次</span>
            </div>
            <div class="stats-item">
              <span class="label">医疗费用</span>
              <span class="value">385亿元</span>
            </div>
          </div>
        </div>

        <div class="tips-card">
          <div class="tips-header">
            <span class="title">使用示例</span>
          </div>
          <div class="tips-list">
            <p>• 去年人口增长率是多少？</p>
            <p>• 哪个区医疗资源最紧张？</p>
            <p>• 建议如何优化床位配置？</p>
            <p>• 异常检测发现了什么问题？</p>
            <p>• 生成月度分析报告</p>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Cpu } from '@element-plus/icons-vue'
import { sendChatMessage, getChatHistory, clearChatHistory, getSuggestQuestions } from '@/api/statistics/ai'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const suggestQuestions = ref([
  '最近三年人口增长趋势如何？',
  '哪个区域医疗资源最紧张？',
  '床位使用率最高的机构是哪个？'
])

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 添加消息
const addMessage = (role, content) => {
  messages.value.push({
    role,
    content,
    time: new Date().toLocaleTimeString()
  })
  scrollToBottom()
}

// 发送消息
const sendMessage = async (text) => {
  const message = text || inputMessage.value.trim()
  if (!message || loading.value) return

  addMessage('user', message)
  inputMessage.value = ''
  loading.value = true

  try {
    const res = await sendChatMessage({ message })
    const reply = res.data?.reply || getMockReply(message)
    addMessage('assistant', reply)
  } catch (error) {
    addMessage('assistant', getMockReply(message))
  } finally {
    loading.value = false
  }
}

// 模拟回复
const getMockReply = (question) => {
  if (question.includes('人口')) {
    return '根据统计数据，近三年人口年均增长率为1.2%，2025年总人口达到1250万人。其中A区人口增长最快，年增长2.1%。'
  }
  if (question.includes('医疗资源') || question.includes('床位')) {
    return '当前全市每千人口床位数6.8张，高于国家标准。A区资源最紧张，每千人仅5.5张床位，建议优先在该区域扩建医疗设施。'
  }
  if (question.includes('异常')) {
    return '最近一周共检测到15起异常记录，其中违规收费10起，异常就诊5起。已处理8起，建议重点关注市人民医院和市中医院的异常情况。'
  }
  if (question.includes('报告')) {
    return '正在为您生成月度分析报告...\n\n【月度报告摘要】\n本月门诊量同比增长4.5%，住院量增长3.2%，医疗费用增长5.8%。床位使用率87.5%，处于合理区间。'
  }
  return '感谢您的提问。根据数据分析，当前全市医疗资源整体供需平衡，但区域分布不均。建议重点关注A区和B区的医疗资源配置优化。如需更详细的分析，请提供具体指标名称。'
}

// 清除历史
const clearHistory = async () => {
  try {
    await clearChatHistory()
    messages.value = []
    addMessage('assistant', '对话历史已清除。有什么可以帮助您的？')
  } catch (error) {
    messages.value = []
    addMessage('assistant', '对话历史已清除。有什么可以帮助您的？')
  }
}

// 加载历史
const loadHistory = async () => {
  try {
    const res = await getChatHistory()
    if (res.data && res.data.length) {
      messages.value = res.data
    } else {
      addMessage('assistant', '您好！我是健康大数据分析助手。您可以向我咨询人口、机构、人员、床位、服务、费用等相关统计数据，我也可以为您提供决策建议。')
    }
  } catch (error) {
    addMessage('assistant', '您好！我是健康大数据分析助手。您可以向我咨询人口、机构、人员、床位、服务、费用等相关统计数据，我也可以为您提供决策建议。')
  }
}

// 加载建议问题
const loadSuggestQuestions = async () => {
  try {
    const res = await getSuggestQuestions()
    if (res.data && res.data.length) {
      suggestQuestions.value = res.data
    }
  } catch (error) {
    // 使用默认问题
  }
}

onMounted(() => {
  loadHistory()
  loadSuggestQuestions()
})
</script>

<style scoped>
.chat-assistant {
  min-height: 500px;
}

.chat-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  height: 600px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.chat-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #409eff;
  color: #fff;
}

.message-content {
  max-width: 70%;
  margin: 0 12px;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  padding: 10px 14px;
  border-radius: 12px;
  background: #f0f2f5;
  font-size: 14px;
  line-height: 1.5;
}

.message.user .message-text {
  background: #409eff;
  color: #fff;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 14px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-10px); opacity: 1; }
}

.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.suggest-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.suggest-tag {
  cursor: pointer;
}

.suggest-tag:hover {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

.stats-card, .tips-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stats-header, .tips-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.stats-header .title, .tips-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stats-list .stats-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stats-list .stats-item .label {
  font-size: 14px;
  color: #666;
}

.stats-list .stats-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.tips-list p {
  font-size: 13px;
  color: #666;
  margin: 8px 0;
  cursor: pointer;
}

.tips-list p:hover {
  color: #409eff;
}
</style>