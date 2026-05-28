<template>
  <div class="page-container">
    <div class="ai-header">
      <h2><el-icon><Cpu /></el-icon> 智能分析中心</h2>
      <p class="subtitle">集成对话大模型、语音交互、图像识别、异常检测等AI能力</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 对话助手 -->
      <el-tab-pane name="chat">
        <template #label><span><el-icon><ChatDotRound /></el-icon> 数据分析助手</span></template>
        <div class="chat-container">
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
      </el-tab-pane>

      <!-- 语音查询 -->
      <el-tab-pane name="asr">
        <template #label><span><el-icon><Microphone /></el-icon> 语音查询</span></template>
        <div class="asr-container"><el-row :gutter="24"><el-col :span="12"><div class="asr-card"><div class="asr-title">语音输入</div><div class="asr-microphone" :class="{ listening: asrListening }"><el-button :type="asrListening ? 'danger' : 'primary'" size="large" circle @click="toggleASR"><el-icon><Microphone v-if="!asrListening" /><Close v-else /></el-icon></el-button><p v-if="!asrListening && !asrText">点击麦克风开始语音输入</p><p v-if="asrListening" class="listening-text">正在聆听...</p><p v-if="asrText && !asrListening" class="asr-text"><el-tag type="success">识别结果</el-tag>{{ asrText }}</p></div></div></el-col><el-col :span="12"><div class="asr-card"><div class="asr-title">查询结果</div><div class="asr-result" v-loading="asrQueryLoading"><div v-if="asrResult" class="result-content"><pre>{{ asrResult }}</pre></div><el-empty v-else description="等待语音查询" /></div></div></el-col></el-row></div>
      </el-tab-pane>

      <!-- 语音播报 -->
      <el-tab-pane name="tts">
        <template #label><span><el-icon><Headset /></el-icon> 语音播报</span></template>
        <div class="tts-container"><el-row :gutter="24"><el-col :span="24"><div class="tts-card"><div class="tts-title">选择报告类型</div><div class="tts-controls"><el-radio-group v-model="ttsReportType"><el-radio-button label="bed">📊 床位统计报告</el-radio-button><el-radio-button label="cost">💰 费用统计报告</el-radio-button><el-radio-button label="service">🏥 服务量报告</el-radio-button></el-radio-group></div></div></el-col><el-col :span="24"><div class="tts-card"><div class="tts-title">报告预览</div><el-input type="textarea" :rows="4" v-model="ttsReportContent" readonly /></div></el-col><el-col :span="24"><div class="tts-card"><div class="tts-actions"><el-button type="primary" size="large" @click="speakReport" :loading="ttsSpeaking">生成并播报</el-button><el-button size="large" @click="stopSpeak">停止播报</el-button></div></div></el-col></el-row></div>
      </el-tab-pane>

      <!-- 图像分类 -->
      <el-tab-pane name="image">
        <template #label><span><el-icon><Picture /></el-icon> 图像分类</span></template>
        <div class="image-container"><el-row :gutter="24"><el-col :span="12"><div class="image-card"><div class="image-title">上传医疗图像</div><el-upload action="#" :before-upload="handleImageUpload" accept="image/*" :show-file-list="false" drag><el-icon><UploadFilled /></el-icon><div class="el-upload__text">将图像拖到此处，或<em>点击上传</em></div></el-upload><div v-if="uploadedImage" class="image-preview"><img :src="uploadedImage"><el-button type="danger" size="small" circle @click="clearImage" class="clear-btn"><el-icon><Close /></el-icon></el-button></div></div></el-col><el-col :span="12"><div class="image-card"><div class="image-title">分类结果</div><div v-if="imageClassifying" v-loading="imageClassifying" class="result-loading">正在分析图像...</div><div v-else-if="imageResult" class="image-result"><div><span class="label">类别：</span><el-tag type="primary">{{ imageResult.category }}</el-tag></div><div><span class="label">置信度：</span><el-progress :percentage="imageResult.confidence" /></div><div><span class="label">描述：</span><p>{{ imageResult.description }}</p></div></div><el-empty v-else description="等待上传图像" /></div></el-col></el-row></div>
      </el-tab-pane>

      <!-- 异常检测 -->
      <el-tab-pane name="detection">
        <template #label><span><el-icon><Warning /></el-icon> 异常检测</span></template>
        <div class="detection-container"><div class="detection-header"><div class="detection-title">异常数据检测 <span class="sub">违规收费 / 异常就诊记录</span></div><div><el-button type="primary" @click="runDetection" :loading="detectLoading">开始检测</el-button></div></div><div class="detection-stats" v-if="anomalyStats.total > 0"><el-row :gutter="16"><el-col :span="8"><div class="stat-item"><div class="stat-value">{{ anomalyStats.total }}</div><div class="stat-label">异常总数</div></div></el-col><el-col :span="8"><div class="stat-item warning"><div class="stat-value">{{ anomalyStats.costAnomaly }}</div><div class="stat-label">违规收费</div></div></el-col><el-col :span="8"><div class="stat-item info"><div class="stat-value">{{ anomalyStats.visitAnomaly }}</div><div class="stat-label">异常就诊</div></div></el-col></el-row></div><el-table v-loading="detectLoading" :data="anomalyList"><el-table-column type="index" label="序号" width="60" /><el-table-column label="记录ID" prop="recordId" /><el-table-column label="机构名称" prop="orgName" /><el-table-column label="异常类型" prop="anomalyType"><template #default="{ row }"><el-tag :type="row.anomalyType === '违规收费' ? 'danger' : 'warning'">{{ row.anomalyType }}</el-tag></template></el-table-column><el-table-column label="描述" prop="description" /><el-table-column label="风险等级" prop="riskLevel"><template #default="{ row }"><el-tag :type="row.riskLevel === '高' ? 'danger' : row.riskLevel === '中' ? 'warning' : 'info'">{{ row.riskLevel }}</el-tag></template></el-table-column><el-table-column label="检测时间" prop="detectTime" /><el-table-column label="操作"><template #default="{ row }"><el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button></template></el-table-column></el-table></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Cpu, ChatDotRound, User, Microphone, Close, Headset, Picture, UploadFilled, Warning } from '@element-plus/icons-vue'

const activeTab = ref('chat')
const chatMessages = ref([{ role: 'assistant', content: '您好！我是数据分析助手，可以帮您查询床位、费用、服务量等数据。', time: new Date().toLocaleTimeString() }])
const chatInput = ref('')
const chatLoading = ref(false)
const asrListening = ref(false)
const asrText = ref('')
const asrResult = ref('')
const asrQueryLoading = ref(false)
const ttsReportType = ref('bed')
const ttsReportContent = ref('')
const ttsSpeaking = ref(false)
const uploadedImage = ref(null)
const imageResult = ref(null)
const imageClassifying = ref(false)
const detectLoading = ref(false)
const anomalyList = ref([])
const anomalyStats = ref({ total: 0, costAnomaly: 0, visitAnomaly: 0 })
let recognition = null

const sendChat = async () => { if (!chatInput.value.trim()) return; chatMessages.value.push({ role: 'user', content: chatInput.value, time: new Date().toLocaleTimeString() }); chatLoading.value = true; setTimeout(() => { chatMessages.value.push({ role: 'assistant', content: '根据最新数据，2025年全市床位总数为140,710张，使用率为61.79%。', time: new Date().toLocaleTimeString() }); chatLoading.value = false; chatInput.value = '' }, 1000) }
const clearChat = () => { chatMessages.value = [{ role: 'assistant', content: '您好！我是数据分析助手，有什么可以帮您？', time: new Date().toLocaleTimeString() }] }
const toggleASR = () => { if (!recognition) return; if (asrListening.value) { recognition.stop(); asrListening.value = false } else { asrText.value = ''; asrResult.value = ''; recognition.start(); asrListening.value = true } }
const speakReport = () => { if (!window.speechSynthesis) return; const utterance = new SpeechSynthesisUtterance(ttsReportContent.value || '请先选择报告类型'); utterance.lang = 'zh-CN'; window.speechSynthesis.speak(utterance); ttsSpeaking.value = true; utterance.onend = () => ttsSpeaking.value = false }
const stopSpeak = () => { window.speechSynthesis?.cancel(); ttsSpeaking.value = false }
const handleImageUpload = (file) => { const reader = new FileReader(); reader.onload = (e) => { uploadedImage.value = e.target.result; imageClassifying.value = true; setTimeout(() => { imageResult.value = { category: 'CT影像', confidence: 92.5, description: '该图像为胸部CT影像，图像质量良好。' }; imageClassifying.value = false }, 2000) }; reader.readAsDataURL(file); return false }
const clearImage = () => { uploadedImage.value = null; imageResult.value = null }
const runDetection = () => { detectLoading.value = true; setTimeout(() => { anomalyList.value = [{ recordId: 'S001', orgName: '市第一人民医院', anomalyType: '违规收费', description: '重复收取CT检查费用', riskLevel: '高', detectTime: '2025-05-28 10:30' }]; anomalyStats.value = { total: 3, costAnomaly: 2, visitAnomaly: 1 }; detectLoading.value = false }, 1500) }
const viewDetail = (row) => { ElMessage.info(`详情：${row.description}`) }

onMounted(() => { if (window.webkitSpeechRecognition) { recognition = new window.webkitSpeechRecognition(); recognition.lang = 'zh-CN'; recognition.onresult = (e) => { asrText.value = e.results[0][0].transcript; asrListening.value = false; setTimeout(() => asrResult.value = `查询结果：根据语音指令"${asrText.value}"，2025年床位使用率为61.79%`, 500) } } })
onBeforeUnmount(() => { recognition?.stop() })
</script>

<style scoped>
.page-container { padding: 20px; }
.ai-header { margin-bottom: 24px; }
.ai-header h2 { margin: 0 0 8px; }
.ai-header .subtitle { color: #909399; font-size: 14px; }
.chat-container { height: 500px; display: flex; flex-direction: column; border: 1px solid #e4e7ed; border-radius: 8px; background: #f5f7fa; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; }
.message { display: flex; margin-bottom: 20px; }
.message.user { flex-direction: row-reverse; }
.message-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #67c23a; color: white; margin-right: 12px; }
.message.user .message-avatar { background: #409eff; margin-left: 12px; margin-right: 0; }
.message-text { padding: 12px 16px; border-radius: 18px; background: white; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
.message.user .message-text { background: #409eff; color: white; }
.message-time { font-size: 12px; color: #909399; margin-top: 6px; }
.typing-indicator { display: flex; gap: 4px; padding: 12px 16px; }
.typing-indicator span { width: 8px; height: 8px; border-radius: 50%; background: #909399; animation: typing 1.4s infinite; }
@keyframes typing { 0%,60%,100% { transform: translateY(0); } 30% { transform: translateY(-8px); } }
.chat-input-area { padding: 16px; background: white; border-top: 1px solid #e4e7ed; }
.chat-actions { margin-top: 12px; display: flex; gap: 12px; }
.asr-card, .tts-card, .image-card { background: #f5f7fa; border-radius: 12px; padding: 24px; margin-bottom: 20px; }
.asr-microphone { text-align: center; padding: 40px 0; }
.asr-microphone .el-button { width: 80px; height: 80px; font-size: 32px; }
.listening-text { margin-top: 20px; color: #f56c6c; }
.tts-actions { display: flex; gap: 16px; justify-content: center; margin-top: 16px; }
.image-preview { position: relative; margin-top: 20px; text-align: center; }
.image-preview img { max-width: 100%; max-height: 300px; border-radius: 8px; }
.clear-btn { position: absolute; top: 8px; right: 8px; }
.detection-stats { background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 12px; padding: 24px; margin-bottom: 20px; color: white; text-align: center; }
.detection-stats .stat-value { font-size: 32px; font-weight: bold; }
</style>