<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="logo-container">
        <h2>健康大数据分析系统</h2>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/statistics/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/statistics/population">
          <el-icon><User /></el-icon>
          <span>人口信息统计分析</span>
        </el-menu-item>
        <el-menu-item index="/statistics/institution">
          <el-icon><OfficeBuilding /></el-icon>
          <span>医疗卫生机构统计分析</span>
        </el-menu-item>
        <el-menu-item index="/statistics/staff">
          <el-icon><UserFilled /></el-icon>
          <span>医疗卫生人员统计分析</span>
        </el-menu-item>
        <el-menu-item index="/statistics/bed">
          <el-icon><List /></el-icon>
          <span>医疗卫生床位统计分析</span>
        </el-menu-item>
        <el-menu-item index="/statistics/service">
          <el-icon><FirstAidKit /></el-icon>
          <span>医疗服务统计分析</span>
        </el-menu-item>
        <el-menu-item index="/statistics/cost">
          <el-icon><Money /></el-icon>
          <span>医疗费用统计分析</span>
        </el-menu-item>
        <el-sub-menu index="/statistics/ai">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>智能算法中心</span>
          </template>
          <el-menu-item index="/statistics/ai/image">图像分类分析</el-menu-item>
          <el-menu-item index="/statistics/ai/anomaly">异常检测</el-menu-item>
          <el-menu-item index="/statistics/ai/chat">智能对话助手</el-menu-item>
          <el-menu-item index="/statistics/ai/voice">语音功能</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-container">
      <div class="navbar">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              admin
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataLine, User, OfficeBuilding, UserFilled, List,
  FirstAidKit, Money, Cpu, CaretBottom
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '统计分析决策系统')

function handleCommand(command) {
  if (command === 'logout') {
    // 退出登录逻辑
    router.push('/login')
  }
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 240px;
  background-color: #304156;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3a4a;
  color: #fff;
}

.logo-container h2 {
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.breadcrumb {
  font-size: 14px;
}

.user-info {
  cursor: pointer;
}

.user-name {
  color: #333;
  display: flex;
  align-items: center;
  gap: 5px;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f6;
}
</style>