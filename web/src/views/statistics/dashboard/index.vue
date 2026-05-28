<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <h1>欢迎使用健康大数据统计分析决策系统</h1>
      <p>您已成功登录系统</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in statsCards" :key="stat.title">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-trend">{{ stat.trend }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, OfficeBuilding, List, Money } from '@element-plus/icons-vue'

const statsCards = ref([
  { title: '总人口', value: '1,250万', trend: '同比增长 1.2%', icon: 'User', color: '#409eff' },
  { title: '医疗机构', value: '3,850家', trend: '同比增长 2.5%', icon: 'OfficeBuilding', color: '#67c23a' },
  { title: '床位数', value: '8.5万张', trend: '同比增长 3.5%', icon: 'List', color: '#e6a23c' },
  { title: '医疗费用', value: '385亿元', trend: '同比增长 5.8%', icon: 'Money', color: '#f56c6c' }
])
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  margin-bottom: 30px;
}

.welcome-card h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.welcome-card p {
  font-size: 14px;
  opacity: 0.8;
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #999;
}
</style>