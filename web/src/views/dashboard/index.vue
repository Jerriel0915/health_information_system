<template>
  <div class="dashboard-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card" @click="goTo(card.path)">
          <div class="card-icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">{{ card.title }}</div>
            <div class="card-value">{{ formatNumber(card.value) }}<span class="unit">{{ card.unit }}</span></div>
            <div class="card-compare" v-if="card.compare !== undefined">
              <el-icon><CaretTop v-if="card.compare > 0" /><CaretBottom v-else /></el-icon>
              {{ Math.abs(card.compare) }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><PieChart /></el-icon> 机构类型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><User /></el-icon> 人员职称分布</span>
          </template>
          <div ref="staffChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><TrendCharts /></el-icon> 服务量趋势</span>
          </template>
          <div ref="serviceChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><Money /></el-icon> 费用构成分析</span>
          </template>
          <div ref="costChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  OfficeBuilding, User, Grid, FirstAidKit, Money, PieChart,
  TrendCharts, Warning, CaretTop, CaretBottom
} from '@element-plus/icons-vue'
import { getDashboardSummary } from '@/api/dashboard'
import { getInstitutionTypeDistribution } from '@/api/system/institution'
import { getStaffJobTitleDistribution } from '@/api/system/staff'
import { getServiceTrend } from '@/api/system/service'
import { getCostComposition } from '@/api/system/cost'

const router = useRouter()

const statCards = ref([])
const typeChartRef = ref(null)
const staffChartRef = ref(null)
const serviceChartRef = ref(null)
const costChartRef = ref(null)
let typeChart = null, staffChart = null, serviceChart = null, costChart = null

const formatNumber = (value) => {
  if (!value && value !== 0) return 0
  if (value >= 10000) return (value / 10000).toFixed(1) + '\u4e07'
  return value.toLocaleString()
}

const goTo = (path) => { if (path) router.push(path) }

// 加载首页概览数据
const loadSummary = async () => {
  try {
    const data = await getDashboardSummary()
    statCards.value = [
      { key: 'institutions', title: '医疗机构', icon: 'OfficeBuilding', value: data.institutionCount || data.totalInstitutions || 0, unit: '家', compare: data.institutionCompare, path: '/system/institution' },
      { key: 'staff', title: '人员总数', icon: 'User', value: data.staffCount || data.totalStaff || 0, unit: '人', compare: data.staffCompare, path: '/system/staff' },
      { key: 'bed', title: '床位总数', icon: 'Grid', value: data.bedCount || data.totalBeds || 0, unit: '张', compare: data.bedCompare, path: '/system/bed' },
      { key: 'service', title: '服务量', icon: 'FirstAidKit', value: data.serviceCount || data.totalServices || 0, unit: '人次', compare: data.serviceCompare, path: '/system/service' }
    ]
  } catch (e) {
    statCards.value = [
      { key: 'institutions', title: '医疗机构', icon: 'OfficeBuilding', value: 0, unit: '家', path: '/system/institution' },
      { key: 'staff', title: '人员总数', icon: 'User', value: 0, unit: '人', path: '/system/staff' },
      { key: 'bed', title: '床位总数', icon: 'Grid', value: 0, unit: '张', path: '/system/bed' },
      { key: 'service', title: '服务量', icon: 'FirstAidKit', value: 0, unit: '人次', path: '/system/service' }
    ]
  }
}

// 机构类型分布
const loadTypeDistribution = async () => {
  try {
    const data = await getInstitutionTypeDistribution()
    if (!typeChartRef.value) return
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie', radius: '55%',
        data: list.length ? list.map(d => ({ name: d.name || d.typeName || d.key, value: d.value || d.count })) : [{ name: '暂无数据', value: 1 }],
        label: { show: true, formatter: '{b}: {d}%' }
      }]
    })
  } catch (e) { /* 静默处理 */ }
}

// 人员职称分布
const loadStaffDistribution = async () => {
  try {
    const data = await getStaffJobTitleDistribution()
    if (!staffChartRef.value) return
    if (staffChart) staffChart.dispose()
    staffChart = echarts.init(staffChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const names = list.map(d => d.name || d.jobTitle || d.key)
    const values = list.map(d => d.value || d.count)
    staffChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: names.length ? names : ['暂无'], axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: '人数' },
      series: [{ type: 'bar', data: values.length ? values : [0], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' } }]
    })
  } catch (e) { /* 静默处理 */ }
}

// 服务量趋势
const loadServiceTrend = async () => {
  try {
    const data = await getServiceTrend()
    if (!serviceChartRef.value) return
    if (serviceChart) serviceChart.dispose()
    serviceChart = echarts.init(serviceChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const years = list.map(d => d.year || d.time || d.date || d.key)
    const values = list.map(d => d.value || d.count || d.serviceCount)
    serviceChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: years.length ? years : ['暂无'], name: '年份' },
      yAxis: { type: 'value', name: '服务量（人次）' },
      series: [{
        type: 'line', data: values.length ? values : [0],
        smooth: true, lineStyle: { color: '#409EFF', width: 3 },
        areaStyle: { opacity: 0.3 }, symbol: 'circle', symbolSize: 8
      }]
    })
  } catch (e) { /* 静默处理 */ }
}

// 费用构成
const loadCostComposition = async () => {
  try {
    const data = await getCostComposition()
    if (!costChartRef.value) return
    if (costChart) costChart.dispose()
    costChart = echarts.init(costChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    costChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: list.length ? list.map(d => ({ name: d.name || d.costCategory || d.key, value: d.value || d.amount || d.cost })) : [{ name: '暂无数据', value: 1 }],
        label: { show: true, formatter: '{b}: {d}%' }
      }]
    })
  } catch (e) { /* 静默处理 */ }
}

const handleResize = () => {
  ;[typeChart, staffChart, serviceChart, costChart].forEach(chart => chart?.resize())
}

onMounted(async () => {
  await loadSummary()
  loadTypeDistribution()
  loadStaffDistribution()
  loadServiceTrend()
  loadCostComposition()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  ;[typeChart, staffChart, serviceChart, costChart].forEach(chart => chart?.dispose())
})
</script>

<style scoped>
.dashboard-container { padding: 20px; background: #f0f2f6; min-height: calc(100vh - 100px); }
.stat-cards { margin-bottom: 24px; }
.stat-card { cursor: pointer; transition: all 0.3s; border-radius: 12px; }
.stat-card:hover { transform: translateY(-6px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.stat-card :deep(.el-card__body) { display: flex; padding: 20px; }
.card-icon { width: 56px; height: 56px; border-radius: 12px; background: #e6f7ff; color: #1890ff; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 28px; }
.card-content { flex: 1; }
.card-title { font-size: 14px; color: #8c8c8c; margin-bottom: 8px; }
.card-value { font-size: 28px; font-weight: bold; color: #262626; line-height: 1.2; }
.unit { font-size: 14px; font-weight: normal; margin-left: 4px; }
.card-compare { font-size: 12px; color: #8c8c8c; margin-top: 8px; }
.card-compare .el-icon { color: #52c41a; }
.chart-row { margin-bottom: 24px; }
</style>
