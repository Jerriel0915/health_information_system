<template>
  <div class="sidebar">
    <div class="logo">
      <h2>健康大数据分析</h2>
    </div>
    <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataLine /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/population">
        <el-icon><User /></el-icon>
        <span>人口信息统计分析</span>
      </el-menu-item>
      <el-menu-item index="/institution">
        <el-icon><OfficeBuilding /></el-icon>
        <span>医疗卫生机构统计分析</span>
      </el-menu-item>
      <el-menu-item index="/staff">
        <el-icon><UserFilled /></el-icon>
        <span>医疗卫生人员统计分析</span>
      </el-menu-item>
      <el-menu-item index="/bed">
        <el-icon><List /></el-icon>
        <span>医疗卫生床位统计分析</span>
      </el-menu-item>
      <el-menu-item index="/service">
        <el-icon><FirstAidKit /></el-icon>
        <span>医疗服务统计分析</span>
      </el-menu-item>
      <el-menu-item index="/cost">
        <el-icon><Money /></el-icon>
        <span>医疗费用统计分析</span>
      </el-menu-item>
      <el-sub-menu index="/ai">
        <template #title>
          <el-icon><Cpu /></el-icon>
          <span>智能算法中心</span>
        </template>
        <el-menu-item index="/ai">算法中心</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataLine, User, OfficeBuilding, UserFilled, List,
  FirstAidKit, Money, Cpu
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  width: 240px;
  background-color: #304156;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
}

.logo h2 {
  font-size: 16px;
  color: #fff;
  margin: 0;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}
</style>