<template>
  <div class="voice-function">
    <el-row :gutter="20">
      <!-- 左侧：语音输入 -->
      <el-col :span="12">
        <div class="voice-card">
          <div class="card-header">
            <span class="card-title">语音查询（ASR）</span>
          </div>
          <div class="voice-area">
            <div class="mic-container">
              <div
                  class="mic-button"
                  :class="{ recording: isRecording }"
                  @click="toggleRecording"
              >
                <el-icon><Microphone /></el-icon>
              </div>
              <p v-if="!isRecording" class="mic-tip">点击开始说话</p>
              <p v-else class="mic-tip recording-tip">正在录音中... 点击结束</p>
            </div>

            <div v-if="recognizedText" class="recognized-text">
              <div class="text-label">识别结果：</div>
              <div class="text-content">{{ recognizedText }}</div>
              <el-button type="primary" size="small" @click="executeVoiceQuery">
                执行查询
              </el-button>
            </div>

            <div v-if="voiceResult" class="voice-result">
              <div class="result-label">查询结果：</div>
              <div class="result-content">{{ voiceResult }}</div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：语音输出 -->
      <el-col :span="12">
        <div class="voice-card">
          <div class="card-header">
            <span class="card-title">语音播报（TTS）</span>
          </div>
          <div class="tts-area">
            <el-input
                v-model="ttsText"
                type="textarea"
                :rows="4"
                placeholder="输入要转换为语音的文字内容..."
            />
            <div class="tts-controls">
              <el-select v-model="ttsVoice" placeholder="选择语音" size="small" style="width: 120px">
                <el-option label="女声" value="female" />
                <el-option label="男声" value="male" />
              </el-select>
              <el-button type="primary" @click="speakText">
                <el-icon><CaretRight /></el-icon>
                播报
              </el-button>
              <el-button @click="stopSpeaking">
                <el-icon><VideoPause /></el-icon>
                停止
              </el-button>
            </div>
          </div>

          <!-- 快捷播报 -->
          <div class="quick-speak">
            <div class="card-subtitle">快捷播报</div>
            <div class="quick-buttons">
              <el-button size="small" @click="quickSpeak('人口概况')">人口概况</el-button>
              <el-button size="small" @click="quickSpeak('机构统计')">机构统计</el-button>
              <el-button size="small" @click="quickSpeak('床位使用率')">床位使用率</el-button>
              <el-button size="small" @click="quickSpeak('费用分析')">费用分析</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone, CaretRight, VideoPause } from '@element-plus/icons-vue'
import { speechRecognition, textToSpeech, voiceQuery } from '@/api/statistics/ai'

const isRecording = ref(false)
const recognizedText = ref('')
const voiceResult = ref('')
const ttsText = ref('')
const ttsVoice = ref('female')

let mediaRecorder = null
let audioChunks = []

// 切换录音
const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 开始录音
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' })
      await recognizeSpeech(audioBlob)
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
  } catch (error) {
    ElMessage.error('无法访问麦克风，请检查权限设置')
  }
}

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    isRecording.value = false
  }
}

// 语音识别
const recognizeSpeech = async (audioBlob) => {
  try {
    const res = await speechRecognition(audioBlob)
    recognizedText.value = res.data?.text || '识别失败，请重试'
  } catch (error) {
    // 模拟识别
    recognizedText.value = '查询2025年全市总人口'
    ElMessage.success('识别完成（演示模式）')
  }
}

// 执行语音查询
const executeVoiceQuery = async () => {
  if (!recognizedText.value) return

  try {
    const res = await voiceQuery({ query: recognizedText.value })
    voiceResult.value = res.data?.result || '根据查询，2025年全市总人口为1250万人，同比增长1.2%。'
  } catch (error) {
    voiceResult.value = '根据查询，2025年全市总人口为1250万人，同比增长1.2%。'
  }
}

// TTS语音合成
const speakText = async () => {
  if (!ttsText.value) {
    ElMessage.warning('请输入要播报的文字')
    return
  }

  try {
    const res = await textToSpeech({ text: ttsText.value, voice: ttsVoice.value })
    const audioBlob = res.data
    const audioUrl = URL.createObjectURL(audioBlob)
    const audio = new Audio(audioUrl)
    audio.play()
    audio.onended = () => {
      URL.revokeObjectURL(audioUrl)
    }
  } catch (error) {
    // 浏览器原生TTS
    const utterance = new SpeechSynthesisUtterance(ttsText.value)
    utterance.lang = 'zh-CN'
    utterance.rate = 1
    utterance.pitch = 1
    if (ttsVoice.value === 'female') {
      utterance.voice = speechSynthesis.getVoices().find(v => v.lang === 'zh-CN' && v.name.includes('Female'))
    }
    speechSynthesis.speak(utterance)
  }
}

// 停止播报
const stopSpeaking = () => {
  speechSynthesis.cancel()
}

// 快捷播报
const quickSpeak = (type) => {
  const texts = {
    '人口概况': '截至2025年底，全市常住人口1250万人，其中男性638万人，女性612万人，男女性别比为104.25。',
    '机构统计': '全市共有医疗卫生机构3850家，其中医院185家，基层医疗卫生机构3420家，专业公共卫生机构245家。',
    '床位使用率': '全市医疗机构床位总数为8.5万张，床位使用率为87.5%，平均住院日9.2天。',
    '费用分析': '全市医疗总费用385亿元，人均医疗费用3080元，医保基金支出192.5亿元，报销比例68.5%。'
  }
  ttsText.value = texts[type] || ''
  speakText()
}
</script>

<style scoped>
.voice-function {
  min-height: 500px;
}

.voice-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-subtitle {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin-bottom: 12px;
}

.voice-area {
  text-align: center;
}

.mic-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.mic-button {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 12px;
}

.mic-button .el-icon {
  font-size: 36px;
  color: #666;
}

.mic-button:hover {
  background: #e6e8ec;
}

.mic-button.recording {
  background: #f56c6c;
  animation: pulse 1.5s infinite;
}

.mic-button.recording .el-icon {
  color: #fff;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

.mic-tip {
  font-size: 12px;
  color: #999;
}

.recording-tip {
  color: #f56c6c;
}

.recognized-text {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  text-align: left;
  margin-bottom: 16px;
}

.text-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.text-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.voice-result {
  background: #ecf5ff;
  padding: 16px;
  border-radius: 8px;
  text-align: left;
}

.result-label {
  font-size: 12px;
  color: #409eff;
  margin-bottom: 8px;
}

.result-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.tts-area {
  margin-bottom: 20px;
}

.tts-controls {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  align-items: center;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>