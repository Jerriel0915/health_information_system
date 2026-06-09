<template>
  <div class="page-container">
    <div class="ai-header">
      <h2><el-icon><Cpu /></el-icon> 智能分析中心</h2>
      <p class="subtitle">集成对话大模型、语音交互、图像识别、异常检测等AI能力</p>
    </div>
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane name="chat">
        <template #label><span><el-icon><ChatDotRound /></el-icon> 数据分析助手</span></template>
        <div class="chat-layout">
          <div class="chat-sidebar">
            <div class="sidebar-header"><span>会话列表</span><el-button type="primary" size="small" @click="handleNewSession" circle><el-icon><Plus /></el-icon></el-button></div>
            <div class="session-list" v-if="sessionList.length">
              <div v-for="s in sessionList" :key="s.id" :class="['session-item', { active: s.id === currentSessionId }]" @click="switchSession(s.id)">
                <div class="session-title">{{ s.title }}</div>
                <div class="session-time">{{ s.updatedAt ? new Date(s.updatedAt).toLocaleString() : '' }}</div>
                <el-button link type="danger" size="small" class="session-del" @click.stop="handleDeleteSession(s.id)"><el-icon><Delete /></el-icon></el-button>
              </div>
            </div>
            <div v-else class="sidebar-empty">暂无会话</div>
          </div>
          <div class="chat-main">
            <div class="chat-messages" ref="chatMessagesRef">
              <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['message', msg.role]">
                <div class="message-avatar"><el-icon><User v-if="msg.role === 'user'" /><Cpu v-else /></el-icon></div>
                <div class="message-content"><div class="message-text">{{ msg.content }}</div><div class="message-time">{{ msg.time }}</div></div>
              </div>
              <div v-if="chatLoading" class="message assistant"><div class="message-avatar"><el-icon><Cpu /></el-icon></div><div class="message-content"><div class="typing-indicator"><span></span><span></span><span></span></div></div></div>
            </div>
            <div class="chat-input-area">
              <el-input v-model="chatInput" type="textarea" :rows="3" placeholder="输入问题，例如：今年床位使用率趋势如何？" @keyup.ctrl.enter="sendChat" />
              <div class="chat-actions"><el-button type="primary" @click="sendChat" :loading="chatLoading">发送 (Ctrl+Enter)</el-button><el-button @click="clearChat">清空对话</el-button></div>
            </div>
          </div>
        </div>
      </el-tab-pane>
            <el-tab-pane name="asr">
        <template #label><span><el-icon><Microphone /></el-icon> 语音查询</span></template>
        <div class="asr-container">
          <el-row :gutter="24">
            <el-col :span="10">
              <el-row :gutter="16">
                <el-col :span="24" style="margin-bottom: 16px;">
                  <div class="asr-card">
                    <div class="asr-title"><el-icon><Microphone /></el-icon> 语音输入</div>
                    <div class="asr-microphone" :class="{ listening: asrListening }">
                      <el-button :type="asrListening ? 'danger' : 'primary'" size="large" circle @click="toggleASR"><el-icon><Microphone v-if="!asrListening" /><Close v-else /></el-icon></el-button>
                      <p v-if="!asrListening">点击麦克风开始语音输入</p>
                      <p v-if="asrListening" class="listening-text">正在聆听...</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="24">
                  <div class="asr-card">
                    <div class="asr-title"><el-icon><UploadFilled /></el-icon> 上传音频文件</div>
                    <div class="asr-upload">
                      <el-upload action="#" accept="audio/*" :show-file-list="false" :before-upload="handleFileUpload">
                        <el-button type="info" size="large"><el-icon><FolderOpened /></el-icon> 选择音频文件</el-button>
                        <p class="upload-hint">支持 wav / mp3 / m4a 格式</p>
                      </el-upload>
                      <p v-if="uploadFileName" class="upload-file-name"><el-tag type="info">{{ uploadFileName }}</el-tag></p>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </el-col>
            <el-col :span="14">
              <div class="asr-card result-card">
                <div class="asr-title"><el-icon><Document /></el-icon> 查询结果</div>
                <div class="asr-result" v-loading="asrQueryLoading">
                  <div v-if="asrQueryResult" class="result-content">
                    <div class="asr-recognized"><span class="label">识别文本：</span><p>{{ asrResult }}</p></div>
                    <el-divider />
                    <div class="asr-llm-reply"><span class="label">AI 回复：</span><p>{{ asrQueryResult }}</p></div>
                  </div>
                  <el-empty v-else-if="!asrQueryLoading" description="等待语音查询" />
                  <div v-else class="loading-text">语音识别中...</div>
                </div>
                <div v-if="asrQueryResult" class="asr-actions">
                  <el-button type="primary" size="large" @click="speakAsrResult" :loading="asrSpeaking">生成并播报</el-button>
                  <el-button size="large" @click="stopAsrSpeak">停止播报</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane><el-tab-pane name="tts">
        <template #label><span><el-icon><Headset /></el-icon> 语音播报</span></template>
        <div class="tts-container">
          <el-row :gutter="24">
            <el-col :span="24">
              <div class="tts-card">
                <div class="tts-title">选择报告类型</div>
                <div class="tts-controls"><el-radio-group v-model="ttsReportType" @change="generateReportContent">
                  <el-radio-button label="bed">床位统计报告</el-radio-button>
                  <el-radio-button label="cost">费用统计报告</el-radio-button>
                  <el-radio-button label="service">服务量报告</el-radio-button>
                </el-radio-group></div>
              </div>
            </el-col>
            <el-col :span="24"><div class="tts-card"><div class="tts-title">报告预览</div><el-input type="textarea" :rows="6" v-model="ttsReportContent" readonly /></div></el-col>
            <el-col :span="24"><div class="tts-card"><div class="tts-actions"><el-button type="primary" size="large" @click="speakReport" :loading="ttsSpeaking">生成并播报</el-button><el-button size="large" @click="stopSpeak">停止播报</el-button></div></div></el-col>
          </el-row>
        </div>
      </el-tab-pane>
      <el-tab-pane name="image">
        <template #label><span><el-icon><Picture /></el-icon> 医疗器械分类测试</span></template>
        <div class="image-container"><el-row :gutter="24">
          <el-col :span="12"><div class="image-card"><div class="image-title">上传医疗图像</div><el-upload action="#" :before-upload="handleImageUpload" accept="image/*" :show-file-list="false" drag><el-icon><UploadFilled /></el-icon><div class="el-upload__text">将图像拖到此处，或<em>点击上传</em></div></el-upload><div v-if="uploadedImage" class="image-preview"><img :src="uploadedImage"><el-button type="danger" size="small" circle @click="clearImage" class="clear-btn"><el-icon><Close /></el-icon></el-button></div></div></el-col>
          <el-col :span="12"><div class="image-card"><div class="image-title">分类结果</div>
            <div v-if="imageClassifying" v-loading="imageClassifying" class="result-loading">正在分析图像...</div>
            <div v-else-if="imageResult" class="image-result">
              <div class="result-header">
                <el-tag type="success" size="large" effect="dark" class="result-tag">{{ categoryMap[imageResult.category] || imageResult.category }}</el-tag>
                <span class="result-conf">{{ imageResult.confidence }}%</span>
              </div>
              <div class="result-desc">{{ imageResult.description }}</div>
              <div class="ranking-title">置信度排名</div>
              <div class="ranking-list">
                <div v-for="(item, idx) in imageResult.ranking" :key="idx" class="ranking-item" :class="{ active: item.category === imageResult.category }">
                  <span class="ranking-index">{{ idx + 1 }}</span>
                  <span class="ranking-name">{{ categoryMap[item.category] || item.category }}</span>
                  <el-progress :percentage="item.confidence" :show-text="false" :stroke-width="6" />
                  <span class="ranking-conf">{{ item.confidence }}%</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="等待上传图像" />
          </div></el-col>
        </el-row></div>
      </el-tab-pane>
            <!-- 肺炎X光分类测试 -->
      <el-tab-pane name="bone">
        <template #label><span><el-icon><Tools /></el-icon> 肺炎X光分类测试</span></template>
        <div class="image-container"><el-row :gutter="24">
          <el-col :span="12"><div class="image-card"><div class="image-title">上传肺部X光图像</div><el-upload action="#" :before-upload="handleBoneUpload" accept="image/*" :show-file-list="false" drag><el-icon><UploadFilled /></el-icon><div class="el-upload__text">将肺部X光图像拖到此处，或<em>点击上传</em></div></el-upload><div v-if="boneUploadedImage" class="image-preview"><img :src="boneUploadedImage"><el-button type="danger" size="small" circle @click="clearBoneImage" class="clear-btn"><el-icon><Close /></el-icon></el-button></div></div></el-col>
          <el-col :span="12"><div class="image-card"><div class="image-title">分类结果</div>
            <div v-if="boneClassifying" v-loading="boneClassifying" class="result-loading">正在分析肺部图像...</div>
            <div v-else-if="boneResult" class="image-result">
              <div class="result-header">
                <el-tag :type="boneResult.class_name === '正常' ? 'success' : 'danger'" size="large" effect="dark" class="result-tag">{{ boneResult.class_name }}</el-tag>
                <span class="result-conf">{{ (boneResult.confidence * 100).toFixed(1) }}%</span>
              </div>
              <div style="text-align:center;padding:10px 0;color:#606266;font-size:14px;">
                {{ boneResult.class_name === '正常' ? '肺部结构未见明显异常' : '检测到肺炎特征，建议进一步检查' }}
              </div>
            </div>
            <el-empty v-else description="等待上传肺部X光图像" />
          </div></el-col>
        </el-row></div>
      </el-tab-pane>
      <el-tab-pane name="detection">
        <template #label><span><el-icon><Warning /></el-icon> 异常检测</span></template>
        <div class="detection-container">
          <div class="detection-header"><div class="detection-title">异常数据检测 <span class="sub">违规收费 / 异常就诊记录</span></div><div><el-button type="primary" @click="runDetection" :loading="detectLoading">开始检测</el-button></div></div>
          <div class="detection-stats" v-if="anomalyStats.total > 0"><el-row :gutter="16">
            <el-col :span="8"><div class="stat-item"><div class="stat-value">{{ anomalyStats.total }}</div><div class="stat-label">异常总数</div></div></el-col>
            <el-col :span="8"><div class="stat-item warning"><div class="stat-value">{{ anomalyStats.costAnomaly }}</div><div class="stat-label">违规收费</div></div></el-col>
            <el-col :span="8"><div class="stat-item info"><div class="stat-value">{{ anomalyStats.visitAnomaly }}</div><div class="stat-label">异常就诊</div></div></el-col>
          </el-row></div>
          <el-table v-loading="detectLoading" :data="anomalyList">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="记录ID" prop="recordId" /><el-table-column label="机构名称" prop="orgName" />
            <el-table-column label="异常类型" prop="anomalyType"><template #default="{ row }"><el-tag :type="row.anomalyType === '违规收费' ? 'danger' : 'warning'">{{ row.anomalyType }}</el-tag></template></el-table-column>
            <el-table-column label="描述" prop="description" />
            <el-table-column label="风险等级" prop="riskLevel"><template #default="{ row }"><el-tag :type="row.riskLevel === '高' ? 'danger' : row.riskLevel === '中' ? 'warning' : 'info'">{{ row.riskLevel }}</el-tag></template></el-table-column>
            <el-table-column label="检测时间" prop="detectTime" />
            <el-table-column label="操作"><template #default="{ row }"><el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Cpu, ChatDotRound, User, Microphone, Close, Headset, Picture, UploadFilled, Warning, FolderOpened, Document, Plus, Delete, Tools } from '@element-plus/icons-vue'
import { sendChatMessage, sendChatMessageStream, speechToText, textToSpeech, getSessionList, createSession, deleteSession, getChatHistory, boneClassify, imageClassify, runAnomalyDetection } from '@/api/ai'

const activeTab = ref('chat')
const chatMessages = ref([{ role: 'assistant', content: '你好！我是数据分析助手，可以帮你查询床位、费用、服务量等数据。', time: new Date().toLocaleTimeString() }])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessagesRef = ref(null)
const currentSessionId = ref('')
const sessionList = ref([])
const userId = ref(1)
const asrListening = ref(false)
const asrText = ref('')
const asrResult = ref('')
const asrQueryLoading = ref(false)
const uploadFileName = ref('')
const ttsReportType = ref('bed')
const ttsReportContent = ref('')
const ttsSpeaking = ref(false)
const uploadedImage = ref(null)
// 医疗设备中英文映射
const categoryMap = {
  'blood pressure set': '血压计',
  'breast pump': '吸奶器',
  'commode': '坐便椅',
  'crutch': '拐杖',
  'glucometer': '血糖仪',
  'oximeter': '血氧仪',
  'rippled mattress': '防褥疮床垫',
  'therapeutic ultrasound machine': '治疗性超声设备',
  'thermometer': '体温计',
  'unknown': '无法识别'
}
const imageResult = ref(null)
const imageClassifying = ref(false)
const detectLoading = ref(false)
const anomalyList = ref([])
const anomalyStats = ref({ total: 0, costAnomaly: 0, visitAnomaly: 0 })
let mediaRecorder = null
let audioChunks = []

const scrollToBottom = async () => {
  await nextTick()
  if (chatMessagesRef.value) chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
}
watch(chatMessages, () => { scrollToBottom() }, { deep: true })

const sendChat = async () => {
  if (!currentSessionId.value) {
    try {
      const res = await createSession(userId.value)
      if (res.code === 200) {
        currentSessionId.value = res.msg
        await loadSessions()
      }
    } catch (e) { console.error('create session failed', e); return }
  }
  if (!chatInput.value.trim()) return
  chatMessages.value.push({ role: 'user', content: chatInput.value, time: new Date().toLocaleTimeString() })
  const q = chatInput.value; chatInput.value = ''; chatLoading.value = true

  // 用固定索引跟踪消息，splice 替换强制触发 Vue 更新
  const msgIndex = chatMessages.value.length
  chatMessages.value.push({ role: 'assistant', content: '', time: new Date().toLocaleTimeString() })

  const streamStartTime = Date.now()
  let streamBuffer = ''
  let typeTimer = null
  let streamDone = false

  const startTypewriter = () => {
    if (typeTimer) return
    typeTimer = setInterval(() => {
      if (streamBuffer.length === 0) {
        if (streamDone) {
          clearInterval(typeTimer)
          typeTimer = null
          finishStream()
        }
        return
      }
      // 每次取1-3个字符
      const take = Math.min(3, streamBuffer.length)
      const chars = streamBuffer.slice(0, take)
      streamBuffer = streamBuffer.slice(take)
      const msg = chatMessages.value[msgIndex]
      if (msg) {
        msg.content += chars
        chatMessages.value.splice(msgIndex, 1, { ...msg })
      }
    }, 30)
  }

  const finishStream = () => {
    if (streamBuffer) {
      const msg = chatMessages.value[msgIndex]
      if (msg) {
        msg.content += streamBuffer
        streamBuffer = ''
        chatMessages.value.splice(msgIndex, 1, { ...msg })
      }
    }
    const elapsed = Date.now() - streamStartTime
    console.log('[Chat] 流式对话完成，耗时:', elapsed, 'ms')
    chatLoading.value = false
    const msg = chatMessages.value[msgIndex]
    if (msg && !msg.content) {
      msg.content = '抱歉，AI 返回为空。'
      chatMessages.value.splice(msgIndex, 1, { ...msg })
    }
  }

  sendChatMessageStream(currentSessionId.value, q,
    (chunk) => {
      streamBuffer += chunk
      startTypewriter()
    },
    () => {
      streamDone = true
    },
    (err) => {
      if (typeTimer) { clearInterval(typeTimer); typeTimer = null }
      streamBuffer = ''
      streamDone = true
      chatLoading.value = false
      const msg = chatMessages.value[msgIndex]
      if (msg) {
        msg.content = 'AI 服务连接失败：' + err
        chatMessages.value.splice(msgIndex, 1, { ...msg })
      }
    }
  )
}

const clearChat = () => { chatMessages.value = [{ role: 'assistant', content: '你好！我是数据分析助手，有什么可以帮你？', time: new Date().toLocaleTimeString() }] }

const loadSessions = async () => {
  try { const res = await getSessionList(userId.value); if (res.code === 200) sessionList.value = res.data || [] } catch (e) { console.error('加载会话列表失败', e) }
}

const handleNewSession = async () => {
  try {
    const res = await createSession(userId.value)
    if (res.code === 200) {
      currentSessionId.value = res.msg
      // 重置聊天区域
      chatMessages.value = [{ role: 'assistant', content: '你好！我是数据分析助手，有什么可以帮你？', time: new Date().toLocaleTimeString() }]
      // 重新加载会话列表并选中新会话
      await loadSessions()
      // 滚动到底部
      scrollToBottom()
    }
  } catch (e) { console.error('创建会话失败', e) }
}

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId
  try {
    const res = await getChatHistory(sessionId)
    if (res.code === 200 && res.data) {
      const msgs = res.data.map(m => ({ role: m.role, content: m.content, time: m.createdAt ? new Date(m.createdAt).toLocaleTimeString() : '' }))
      chatMessages.value = msgs.length ? msgs : [{ role: 'assistant', content: '你好！我是数据分析助手，有什么可以帮你？', time: new Date().toLocaleTimeString() }]
    }
  } catch (e) { console.error('加载历史消息失败', e) }
}

const handleDeleteSession = async (sessionId) => {
  try {
    await deleteSession(sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = ''
      // 重新加载会话列表
      await loadSessions()
      // 如果有其他会话，自动选择最后一个
      if (sessionList.value.length > 0) {
        await switchSession(sessionList.value[sessionList.value.length - 1].id)
      } else {
        // 没有会话了，重置聊天区域
        chatMessages.value = [{ role: 'assistant', content: '你好！我是数据分析助手，有什么可以帮你？', time: new Date().toLocaleTimeString() }]
      }
    } else {
      await loadSessions()
    }
  } catch (e) { console.error('删除会话失败', e) }
}

const asrQueryResult = ref('')
const asrSpeaking = ref(false)

const processAsrResult = async (text) => {
  if (!text || text === '暂无识别结果') { asrQueryLoading.value = false; return }
  asrResult.value = text
  if (!currentSessionId.value) {
    try {
      const cres = await createSession(userId.value)
      if (cres.code === 200) { currentSessionId.value = cres.msg; await loadSessions() }
    } catch (e) { console.error('create session failed', e); asrQueryLoading.value = false; return }
  }
  try {
    const res = await sendChatMessage(currentSessionId.value, text)
    if (res.code === 200) { asrQueryResult.value = res.msg }
    else { asrQueryResult.value = '抱歉，AI 服务暂时不可用。' }
  } catch (e) {
    asrQueryResult.value = 'AI 服务连接失败，请稍后重试。'
  }
  asrQueryLoading.value = false
}

const toggleASR = async () => {
  if (asrListening.value) { mediaRecorder?.stop(); asrListening.value = false; return }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream); audioChunks = []
    mediaRecorder.ondataavailable = (e) => audioChunks.push(e.data)
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach(t => t.stop())
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' }); asrQueryLoading.value = true; asrQueryResult.value = ''
      try {
        const res = await speechToText(audioBlob)
        if (res.code === 200) {
          let text = ''
          try { const parsed = JSON.parse(res.msg); text = parsed.sentences ? parsed.sentences.map(s => s.text).filter(Boolean).join('') : res.msg }
          catch (e) { text = res.msg }
          await processAsrResult(text || '暂无识别结果')
        } else { ElMessage.error(res.msg || '语音识别失败'); asrQueryLoading.value = false }
      } catch (e) { ElMessage.error('语音识别服务连接失败'); asrQueryLoading.value = false }
    }
    mediaRecorder.start(); asrListening.value = true; asrText.value = ''; asrResult.value = ''; asrQueryResult.value = ''
  } catch (e) { ElMessage.error('无法获取麦克风权限') }
}

const handleFileUpload = (file) => {
  uploadFileName.value = file.name; asrQueryLoading.value = true; asrQueryResult.value = ''
  speechToText(file).then(async (res) => {
    if (res.code === 200) {
      let text = ''
      try { const parsed = JSON.parse(res.msg); text = parsed.sentences ? parsed.sentences.map(s => s.text).filter(Boolean).join('') : res.msg }
      catch (e) { text = res.msg }
      await processAsrResult(text || '暂无识别结果')
    } else { ElMessage.error(res.msg || '语音识别失败'); asrQueryLoading.value = false }
  }).catch(e => { ElMessage.error('语音识别服务连接失败: ' + e.message); asrQueryLoading.value = false })
  return false
}

const speakAsrResult = async () => {
  if (!asrQueryResult.value) { ElMessage.warning('没有可播报的内容'); return }
  asrSpeaking.value = true
  try {
    const res = await textToSpeech(asrQueryResult.value)
    if (res instanceof Blob) {
      const url = URL.createObjectURL(res); const audio = new Audio(url)
      audio.onended = () => { asrSpeaking.value = false; URL.revokeObjectURL(url) }
      audio.onerror = () => { asrSpeaking.value = false; URL.revokeObjectURL(url); ElMessage.error('播放失败') }
      audio.play()
    } else { ElMessage.error('语音合成失败'); asrSpeaking.value = false }
  } catch (e) { ElMessage.error('语音合成服务连接失败'); asrSpeaking.value = false }
}

const stopAsrSpeak = () => { window.speechSynthesis?.cancel(); asrSpeaking.value = false }

const generateReportContent = () => {
  const now = new Date(); const ds = now.toLocaleDateString('zh-CN')
  switch (ttsReportType.value) {
    case 'bed': ttsReportContent.value = '【' + ds + ' 床位统计报告】\n全市医疗卫生机构床位总数 1655 张，其中公立医院 1200 张，基层医疗机构 455 张。\n床位使用率 78.5%，较上月提升 2.3 个百分点。\n建议：加强基层医疗资源配置，适当增加康复护理床位。'; break
    case 'cost': ttsReportContent.value = '【' + ds + ' 费用统计报告】\n本周期医疗费用总额约 4253 万元，门诊次均费用 285 元，住院次均费用 6280 元。\n医保基金支出占比 65.2%，个人自付比例 22.8%。\n建议：关注药品费用增长趋势，优化控费措施。'; break
    case 'service': ttsReportContent.value = '【' + ds + ' 服务量统计报告】\n本周期医疗卫生服务总量约 50000 人次，其中门诊服务 38000 人次，住院服务 12000 人次。\n基层首诊比例 42.3%，同比增长 5.1 个百分点。\n建议：持续推进分级诊疗制度，提升基层服务能力。'; break
  }
}

const speakReport = async () => {
  if (!ttsReportContent.value) { ElMessage.warning('请先生成报告内容'); return }
  ttsSpeaking.value = true
  try {
    const res = await textToSpeech(ttsReportContent.value)
    if (res instanceof Blob) {
      const url = URL.createObjectURL(res); const audio = new Audio(url)
      audio.onended = () => { ttsSpeaking.value = false; URL.revokeObjectURL(url) }
      audio.onerror = () => { ttsSpeaking.value = false; URL.revokeObjectURL(url); ElMessage.error('播放失败') }
      audio.play()
    } else { ElMessage.error('语音合成失败'); ttsSpeaking.value = false }
  } catch (e) { ElMessage.error('语音合成服务连接失败'); ttsSpeaking.value = false }
}

const stopSpeak = () => { window.speechSynthesis?.cancel(); ttsSpeaking.value = false }

const handleImageUpload = async (file) => {
  uploadedImage.value = URL.createObjectURL(file)
  imageClassifying.value = true
  imageResult.value = null
  try {
    const res = await imageClassify(file)
    if (res.code === 200) {
      imageResult.value = res.data
    } else {
      ElMessage.error(res.msg || '分类失败')
    }
  } catch (e) {
    ElMessage.error('分类请求异常: ' + (e.message || '未知错误'))
  } finally {
    imageClassifying.value = false
  }
  return false
}
const clearImage = () => { uploadedImage.value = null; imageResult.value = null }
// ==================== 骨骼分类 ====================
const boneUploadedImage = ref(null)
const boneClassifying = ref(false)
const boneResult = ref(null)

const handleBoneUpload = async (file) => {
  boneUploadedImage.value = URL.createObjectURL(file)
  boneClassifying.value = true
  boneResult.value = null
  try {
    const res = await boneClassify(file)
    if (res.code === 200) {
      boneResult.value = res.data
    } else {
      ElMessage.error(res.msg || '骨骼分类失败')
    }
  } catch (e) {
    ElMessage.error('骨骼分类请求异常: ' + (e.message || '未知错误'))
  } finally {
    boneClassifying.value = false
  }
  return false
}
const clearBoneImage = () => { boneUploadedImage.value = null; boneResult.value = null }

const runDetection = async () => {
  detectLoading.value = true
  anomalyList.value = []
  anomalyStats.value = { total: 0, costAnomaly: 0, visitAnomaly: 0 }
  try {
    const res = await runAnomalyDetection()
    if (res.code === 200) {
      anomalyList.value = res.data?.list || []
      anomalyStats.value = res.data?.stats || { total: 0, costAnomaly: 0, visitAnomaly: 0 }
    } else {
      ElMessage.error(res.msg || '异常检测失败')
    }
  } catch (e) {
    ElMessage.error('异常检测请求异常: ' + (e.message || '未知错误'))
  } finally {
    detectLoading.value = false
  }
}
const viewDetail = (row) => { ElMessage.info('异常检测功能待后续更新') }

onMounted(async () => {
  await loadSessions()
  if (sessionList.value.length > 0) await switchSession(sessionList.value[sessionList.value.length - 1].id)
  generateReportContent()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.ai-header { margin-bottom: 24px; }
.ai-header h2 { margin: 0 0 8px; }
.ai-header .subtitle { color: #909399; font-size: 14px; }

.chat-layout { display: flex; height: 600px; border: 1px solid #e4e7ed; border-radius: 8px; overflow: hidden; }
.chat-sidebar { width: 220px; min-width: 220px; background: #f5f7fa; border-right: 1px solid #e4e7ed; display: flex; flex-direction: column; }
.sidebar-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #e4e7ed; font-size: 14px; font-weight: bold; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; margin-bottom: 4px; position: relative; transition: background 0.2s; display: flex; flex-direction: column; }
.session-item:hover { background: #e8eaed; }
.session-item.active { background: #409eff15; border-left: 3px solid #409eff; }
.session-title { font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 24px; }
.session-time { font-size: 11px; color: #909399; margin-top: 2px; }
.session-del { position: absolute; right: 4px; top: 50%; transform: translateY(-50%); display: none; }
.session-item:hover .session-del { display: inline-flex; }
.sidebar-empty { flex: 1; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 13px; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; }
.message { display: flex; margin-bottom: 20px; }
.message.user { flex-direction: row-reverse; }
.message-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #67c23a; color: white; margin-right: 12px; flex-shrink: 0; }
.message.user .message-avatar { background: #409eff; margin-left: 12px; margin-right: 0; }
.message-content { max-width: 70%; }
.message-text { padding: 12px 16px; border-radius: 18px; background: white; box-shadow: 0 1px 2px rgba(0,0,0,0.1); word-break: break-word; white-space: pre-wrap; }
.message.user .message-text { background: #409eff; color: white; }
.message-time { font-size: 12px; color: #909399; margin-top: 6px; }
.typing-indicator { display: flex; gap: 4px; padding: 12px 16px; }
.typing-indicator span { width: 8px; height: 8px; border-radius: 50%; background: #909399; animation: typing 1.4s infinite; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%,60%,100% { transform: translateY(0); } 30% { transform: translateY(-8px); } }
.chat-input-area { padding: 16px; background: white; border-top: 1px solid #e4e7ed; }
.chat-actions { margin-top: 12px; display: flex; gap: 12px; }

.asr-card, .tts-card, .image-card { background: #f5f7fa; border-radius: 12px; padding: 24px; margin-bottom: 20px; }
.asr-title, .tts-title, .image-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; display: flex; align-items: center; gap: 6px; }
.asr-microphone { text-align: center; padding: 30px 0; }
.asr-microphone .el-button { width: 72px; height: 72px; font-size: 28px; }
.asr-upload { text-align: center; padding: 16px 0; }
.upload-hint { font-size: 12px; color: #909399; margin-top: 8px; }
.upload-file-name { margin-top: 10px; }
.result-card { min-height: 400px; display: flex; flex-direction: column; }
.result-card .asr-result { flex: 1; display: flex; align-items: center; justify-content: center; }
.result-content pre { white-space: pre-wrap; word-break: break-word; margin: 0; font-family: inherit; font-size: 14px; color: #303133; }
.listening-text { margin-top: 20px; color: #f56c6c; font-size: 14px; }
.asr-recognized, .asr-llm-reply { margin-bottom: 8px; }
.asr-recognized .label, .asr-llm-reply .label { font-weight: 600; color: #606266; display: block; margin-bottom: 4px; }
.asr-recognized p, .asr-llm-reply p { margin: 0; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.asr-actions { margin-top: 16px; display: flex; gap: 12px; justify-content: center; }
.loading-text { color: #909399; font-size: 14px; }

.tts-controls { margin-bottom: 16px; }
.tts-actions { display: flex; gap: 16px; justify-content: center; margin-top: 16px; }

.image-preview { position: relative; margin-top: 20px; text-align: center; }
.image-preview img { max-width: 100%; max-height: 300px; border-radius: 8px; }
.clear-btn { position: absolute; top: 8px; right: 8px; }
.result-loading { min-height: 200px; display: flex; align-items: center; justify-content: center; }
.result-header { text-align: center; padding: 10px 0 16px; }
.result-tag { font-size: 18px; padding: 8px 24px; }
.result-conf { display: block; font-size: 28px; font-weight: 700; color: #67c23a; margin-top: 8px; }
.result-desc { font-size: 13px; color: #606266; line-height: 1.6; padding: 10px 0 16px; border-bottom: 1px solid #eee; margin-bottom: 12px; }
.ranking-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 10px; }
.ranking-list { display: flex; flex-direction: column; gap: 8px; }
.ranking-item { display: flex; align-items: center; gap: 10px; padding: 6px 10px; border-radius: 8px; background: #f5f7fa; }
.ranking-item.active { background: #e6f7e6; }
.ranking-index { width: 18px; height: 18px; border-radius: 50%; background: #dcdfe6; color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ranking-item.active .ranking-index { background: #67c23a; }
.ranking-name { font-size: 13px; color: #303133; width: 120px; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ranking-item .el-progress { flex: 1; min-width: 0; }
.ranking-conf { font-size: 12px; color: #909399; width: 45px; text-align: right; flex-shrink: 0; }

.detection-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.detection-title { font-size: 18px; font-weight: 600; }
.detection-title .sub { font-size: 13px; font-weight: normal; color: #909399; margin-left: 8px; }
.detection-stats { background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 12px; padding: 24px; margin-bottom: 20px; color: white; text-align: center; }
.detection-stats .stat-value { font-size: 32px; font-weight: bold; }
.detection-stats .stat-label { font-size: 14px; opacity: 0.85; margin-top: 4px; }
</style>












