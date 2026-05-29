<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="logo">
        <span>健康大数据系统</span>
      </div>
      <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页看板</span>
        </el-menu-item>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/institution">
            <el-icon><OfficeBuilding /></el-icon>
            <span>医疗机构</span>
          </el-menu-item>
          <el-menu-item index="/system/staff">
            <el-icon><User /></el-icon>
            <span>人员管理</span>
          </el-menu-item>
          <el-menu-item index="/system/bed">
            <el-icon><Grid /></el-icon>
            <span>床位管理</span>
          </el-menu-item>
          <el-menu-item index="/system/service">
            <el-icon><FirstAidKit /></el-icon>
            <span>服务管理</span>
          </el-menu-item>
          <el-menu-item index="/system/cost">
            <el-icon><Money /></el-icon>
            <span>费用管理</span>
          </el-menu-item>
          <el-menu-item index="/system/population">
            <el-icon><UserFilled /></el-icon>
            <span>人口统计</span>
          </el-menu-item>
          <el-menu-item index="/system/region">
            <el-icon><Location /></el-icon>
            <span>区域管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/ai">
          <el-icon><Cpu /></el-icon>
          <span>智能分析</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-container">
      <div class="navbar">
        <div class="right-menu">
          <span class="username">
            <el-icon><User /></el-icon>
            管理员
          </span>
        </div>
      </div>
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  HomeFilled,
  Setting,
  OfficeBuilding,
  User,
  Grid,
  FirstAidKit,
  Money,
  UserFilled,
  Location,
  Cpu
} from '@element-plus/icons-vue'
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}
.sidebar-container {
  width: 220px;
  background: #304156;
  color: white;
  flex-shrink: 0;
}
.logo {
  height: 50px;
  line-height: 50px;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: white;
  background: #2b3a4a;
  border-bottom: 1px solid #1f2d3a;
}
.el-menu-vertical {
  border-right: none;
  background-color: #304156;
}
.el-menu-vertical :deep(.el-menu-item),
.el-menu-vertical :deep(.el-sub-menu__title) {
  color: #bfcbd9;
}
.el-menu-vertical :deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #263445;
}
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.navbar {
  height: 50px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}
.username {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}
.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f0f2f6;
}
</style>