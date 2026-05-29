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
            <el-button text size="small" style="float: right;" @click="refreshChart('type')">刷新</el-button>
          </template>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><User /></el-icon> 人员职称分布</span>
            <el-button text size="small" style="float: right;" @click="refreshChart('staff')">刷新</el-button>
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
            <el-button text size="small" style="float: right;" @click="refreshChart('service')">刷新</el-button>
          </template>
          <div ref="serviceChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><Money /></el-icon> 费用构成分析</span>
            <el-button text size="small" style="float: right;" @click="refreshChart('cost')">刷新</el-button>
          </template>
          <div ref="costChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon><Warning /></el-icon> 异常预警</span>
            <el-button text size="small" style="float: right;" @click="$router.push('/ai/index?tab=detection')">查看更多</el-button>
          </template>
          <div class="alert-list">
            <div v-for="alert in alertList" :key="alert.id" class="alert-item" :class="alert.level">
              <el-icon><CircleClose v-if="alert.level === 'high'" /><Warning v-else-if="alert.level === 'medium'" /><InfoFilled v-else /></el-icon>
              <span class="alert-content">{{ alert.content }}</span>
              <span class="alert-time">{{ alert.time }}</span>
            </div>
            <div v-if="alertList.length === 0" class="no-alert">
              <el-icon><SuccessFilled /></el-icon> 暂无异常预警
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, markRaw, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  OfficeBuilding, User, Grid, FirstAidKit, Money, PieChart,
  TrendCharts, Warning, CircleClose, InfoFilled, SuccessFilled,
  CaretTop, CaretBottom
} from '@element-plus/icons-vue'
import { getDashboardSummary } from '@/api/dashboard'
import { getInstitutionTypeDistribution, getInstitutionTrend } from '@/api/system/institution'
import { getStaffJobTitleDistribution, getStaffTrend } from '@/api/system/staff'
import { getServiceTrend } from '@/api/system/service'
import { getCostComposition, getCostTrend } from '@/api/system/cost'

const router = useRouter()

// 统计卡片数据
const statCards = ref([
    { key: 'institutions', title: '医疗机构', icon: markRaw(OfficeBuilding), value: 0, unit: '家', path: '/system/institution' },
    { key: 'staff', title: '人员总数', icon: markRaw(User), value: 0, unit: '人', path: '/system/staff' },
    { key: 'bed', title: '床位总数', icon: markRaw(Grid), value: 0, unit: '张', path: '/system/bed' },
    { key: 'service', title: '服务量', icon: markRaw(FirstAidKit), value: 0, unit: '人次', path: '/system/service' }
])

// 异常预警（暂用占位，后续可从后端获取）
const alertList = ref([])

// ECharts 实例
const typeChartRef = ref(null)
const staffChartRef = ref(null)
const serviceChartRef = ref(null)
const costChartRef = ref(null)
let typeChart = null
let staffChart = null
let serviceChart = null
let costChart = null

const formatNumber = (value) => {
  if (!value && value !== 0) return '--'
  if (value >= 10000) return (value / 10000).toFixed(1) + '万'
  return value.toLocaleString()
}

const goTo = (path) => { if (path) router.push(path) }

// 加载统计卡片数据
const loadSummary = async () => {
  try {
    const token = document.cookie.split(';').find(c => c.trim().startsWith('Admin-Token='))
    console.log('[Dashboard] cookie token:', token ? token.substring(0, 40) + '...' : 'EMPTY')
    const res = await getDashboardSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      statCards.value = [
    { key: 'institutions', title: '医疗机构', icon: markRaw(OfficeBuilding), value: d.totalInstitutions || 0, unit: '家', path: '/system/institution' },
    { key: 'staff', title: '人员总数', icon: markRaw(User), value: d.totalStaff || 0, unit: '人', path: '/system/staff' },
    { key: 'bed', title: '床位总数', icon: markRaw(Grid), value: d.totalBedCount || 0, unit: '张', path: '/system/bed' },
    { key: 'service', title: '服务量', icon: markRaw(FirstAidKit), value: d.totalServices || 0, unit: '人次', path: '/system/service' }
      ]
    }
  } catch (e) {
    console.error('加载首页统计失败', e)
  }
}

// 图表渲染 — 机构类型分布
const renderTypeChart = async () => {
  if (!typeChartRef.value) return
  try {
    const res = await getInstitutionTypeDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie', radius: '55%',
        data: data.length ? data : [{ name: '暂无数据', value: 1 }],
        label: { show: true, formatter: '{b}: {d}%' }
      }]
    })
  } catch (e) {
    console.error('加载机构类型分布失败', e)
  }
}

// 图表渲染 — 人员职称分布
const renderStaffChart = async () => {
  if (!staffChartRef.value) return
  try {
    const res = await getStaffJobTitleDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (staffChart) staffChart.dispose()
    staffChart = echarts.init(staffChartRef.value)
    staffChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.name) || [] },
      yAxis: { type: 'value', name: '人数' },
      series: [{ type: 'bar', data: data.map(d => d.value) || [], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' } }]
    })
  } catch (e) {
    console.error('加载人员职称分布失败', e)
  }
}

// 图表渲染 — 服务量趋势
const renderServiceChart = async () => {
  if (!serviceChartRef.value) return
  try {
    const res = await getServiceTrend()
    const data = res.code === 200 ? (res.data || []) : []
    if (serviceChart) serviceChart.dispose()
    serviceChart = echarts.init(serviceChartRef.value)
    serviceChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(d => d.year || d.name) || [], name: '年份' },
      yAxis: { type: 'value', name: '服务量（人次）' },
      series: [{
        type: 'line', data: data.map(d => d.value) || [],
        smooth: true, lineStyle: { color: '#409EFF', width: 3 },
        areaStyle: { opacity: 0.3 }, symbol: 'circle', symbolSize: 8
      }]
    })
  } catch (e) {
    console.error('加载服务量趋势失败', e)
  }
}

// 图表渲染 — 费用构成分析
const renderCostChart = async () => {
  if (!costChartRef.value) return
  try {
    const res = await getCostComposition()
    const data = res.code === 200 ? (res.data || {}) : {}
    if (costChart) costChart.dispose()
    costChart = echarts.init(costChartRef.value)
    const chartData = [
      { name: '药品费', value: data.drugCost || 0 },
      { name: '治疗费', value: data.treatmentCost || 0 },
      { name: '检查费', value: data.inspectionCost || 0 },
      { name: '手术费', value: data.surgeryCost || 0 },
      { name: '床位费', value: data.bedCost || 0 },
      { name: '护理费', value: data.nursingCost || 0 }
    ].filter(d => d.value > 0)
    costChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: chartData.length ? chartData : [{ name: '暂无数据', value: 1 }],
        label: { show: true, formatter: '{b}: {d}%' }
      }]
    })
  } catch (e) {
    console.error('加载费用构成失败', e)
  }
}

const refreshChart = (type) => {
  if (type === 'type') renderTypeChart()
  if (type === 'staff') renderStaffChart()
  if (type === 'service') renderServiceChart()
  if (type === 'cost') renderCostChart()
}

const handleResize = () => {
  typeChart?.resize()
  staffChart?.resize()
  serviceChart?.resize()
  costChart?.resize()
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([
    renderTypeChart(),
    renderStaffChart(),
    renderServiceChart(),
    renderCostChart()
  ])
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
.alert-list { max-height: 280px; overflow-y: auto; }
.alert-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.alert-item:last-child { border-bottom: none; }
.alert-item.high .el-icon { color: #f5222d; margin-right: 12px; }
.alert-item.medium .el-icon { color: #fa8c16; margin-right: 12px; }
.alert-item.low .el-icon { color: #1890ff; margin-right: 12px; }
.alert-content { flex: 1; font-size: 14px; }
.alert-time { font-size: 12px; color: #8c8c8c; }
.no-alert { text-align: center; padding: 40px 0; color: #52c41a; }
.no-alert .el-icon { font-size: 48px; margin-bottom: 12px; display: block; }
</style>

