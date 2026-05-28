<template>
  <div class="ai-container">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" type="border-card" class="ai-tabs">
      <!-- 1. 图像分类分析 -->
      <el-tab-pane name="image" class="tab-pane">
        <template #label>
          <span class="tab-label">
            <el-icon><Picture /></el-icon>
            图像分类分析
          </span>
        </template>
        <ImageClassify />
      </el-tab-pane>

      <!-- 2. 异常检测 -->
      <el-tab-pane name="anomaly">
        <template #label>
          <span class="tab-label">
            <el-icon><Warning /></el-icon>
            异常检测
          </span>
        </template>
        <AnomalyDetection />
      </el-tab-pane>

      <!-- 3. 智能对话助手 -->
      <el-tab-pane name="chat">
        <template #label>
          <span class="tab-label">
            <el-icon><ChatDotRound /></el-icon>
            智能对话助手
          </span>
        </template>
        <ChatAssistant />
      </el-tab-pane>

      <!-- 4. 语音功能 -->
      <el-tab-pane name="voice">
        <template #label>
          <span class="tab-label">
            <el-icon><Microphone /></el-icon>
            语音功能
          </span>
        </template>
        <VoiceFunction />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Picture, Warning, ChatDotRound, Microphone } from '@element-plus/icons-vue'
import ImageClassify from './components/ImageClassify.vue'
import AnomalyDetection from './components/AnomalyDetection.vue'
import ChatAssistant from './components/ChatAssistant.vue'
import VoiceFunction from './components/VoiceFunction.vue'

const activeTab = ref('image')
</script>

<style scoped>
.ai-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.ai-tabs {
  height: 100%;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-pane {
  padding: 20px 0;
  min-height: 500px;
}
</style>