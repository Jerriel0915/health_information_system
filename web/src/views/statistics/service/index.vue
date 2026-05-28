<template>
  <div class="service-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="统计年份">
          <el-select v-model="queryParams.year" placeholder="请选择年份" clearable>
            <el-option
                v-for="year in yearOptions"
                :key="year"
                :label="year"
                :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="统计月份">
          <el-select v-model="queryParams.month" placeholder="请选择月份" clearable>
            <el-option label="全年" value="" />
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-cascader
              v-model="queryParams.region"
              :options="regionOptions"
              :props="{ checkStrictly: true }"
              placeholder="请选择区域"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 核心指标卡片 -->
    <div class="indicator-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">总诊疗人次</div>
            <div class="indicator-value">{{ formatNumber(overviewData.totalVisits) }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.visitsGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.visitsGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">门诊人次</div>
            <div class="indicator-value">{{ formatNumber(overviewData.outpatientVisits) }}</div>
            <div class="indicator-sub">占比 {{ formatPercent(overviewData.outpatientRatio) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">出院人数</div>
            <div class="indicator-value">{{ formatNumber(overviewData.discharges) }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.dischargesGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.dischargesGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">手术人次</div>
            <div class="indicator-value">{{ formatNumber(overviewData.surgeries) }}</div>
            <div class="indicator-sub">住院手术占比 {{ formatPercent(overviewData.inpatientSurgeryRatio) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">急诊人次</div>
          <div class="indicator-value">{{ formatNumber(overviewData.emergencyVisits) }}</div>
          <div class="indicator-sub">急诊抢救成功率 {{ overviewData.rescueSuccessRate || '--' }}%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">平均住院日</div>
          <div class="indicator-value">{{ overviewData.averageHospitalStay || '--' }} 天</div>
          <div class="indicator-sub">较去年 {{ formatChange(overviewData.stayChange) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">床位周转次数</div>
          <div class="indicator-value">{{ overviewData.bedTurnoverRate || '--' }}</div>
          <div class="indicator-sub">次/年</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">医师日均诊疗</div>
          <div class="indicator-value">{{ overviewData.dailyVisitsPerDoctor || '--' }}</div>
          <div class="indicator-sub">人次/日</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 门诊/住院量趋势 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">门诊与住院服务量趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 各月门诊量 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">各月门诊量分布</span>
          </div>
          <div ref="monthlyChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 手术类型分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">手术类型分布</span>
          </div>
          <div ref="surgeryChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 科室服务量排行 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">科室门诊量排行（TOP10）</span>
          </div>
          <div ref="departmentChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 服务效率指标 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">服务效率指标对比</span>
          </div>
          <div ref="efficiencyChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 住院服务趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">住院服务趋势</span>
          </div>
          <div ref="inpatientChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 手术量趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">手术量趋势</span>
          </div>
          <div ref="surgeryTrendChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 机构服务详情表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">医疗机构服务详情</span>
      </div>
      <el-table :data="serviceList" v-loading="loading" stripe>
        <el-table-column prop="institutionName" label="机构名称" min-width="180" />
        <el-table-column prop="institutionType" label="机构类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.institutionType === '医院' ? 'primary' : 'success'">
              {{ scope.row.institutionType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outpatientVisits" label="门诊量" sortable width="120">
          <template #default="scope">
            {{ formatNumber(scope.row.outpatientVisits) }}
          </template>
        </el-table-column>
        <el-table-column prop="emergencyVisits" label="急诊量" sortable width="120">
          <template #default="scope">
            {{ formatNumber(scope.row.emergencyVisits) }}
          </template>
        </el-table-column>
        <el-table-column prop="inpatientVisits" label="住院量" sortable width="120">
          <template #default="scope">
            {{ formatNumber(scope.row.inpatientVisits) }}
          </template>
        </el-table-column>
        <el-table-column prop="discharges" label="出院人数" sortable width="120">
          <template #default="scope">
            {{ formatNumber(scope.row.discharges) }}
          </template>
        </el-table-column>
        <el-table-column prop="surgeries" label="手术量" sortable width="120">
          <template #default="scope">
            {{ formatNumber(scope.row.surgeries) }}
          </template>
        </el-table-column>
        <el-table-column prop="averageHospitalStay" label="平均住院日" sortable width="110" />
        <el-table-column prop="bedTurnoverRate" label="床位周转次数" sortable width="120" />
      </el-table>
      <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getServiceOverview,
  getOutpatientStatistics,
  getInpatientStatistics,
  getSurgeryStatistics,
  getServiceEfficiency,
  getDepartmentRanking,
  getServiceTrend,
  getServiceList
} from '@/api/statistics/service'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  year: new Date().getFullYear(),
  month: '',
  region: [],
  pageNum: 1,
  pageSize: 10
})

// 年份选项
const yearOptions = ref([])
for (let i = 2017; i <= new Date().getFullYear(); i++) {
  yearOptions.value.push(i)
}

// 区域选项
const regionOptions = ref([
  { value: 'city', label: '全市', children: [
      { value: 'district1', label: 'A区' },
      { value: 'district2', label: 'B区' },
      { value: 'district3', label: 'C区' },
      { value: 'district4', label: 'D区' },
      { value: 'district5', label: 'E区' },
      { value: 'district6', label: 'F区' }
    ]}
])

// 数据存储
const overviewData = ref({})
const serviceList = ref([])
const total = ref(0)

// 图表实例
const trendChartRef = ref(null)
const monthlyChartRef = ref(null)
const surgeryChartRef = ref(null)
const departmentChartRef = ref(null)
const efficiencyChartRef = ref(null)
const inpatientChartRef = ref(null)
const surgeryTrendChartRef = ref(null)
let trendChart = null
let monthlyChart = null
let surgeryChart = null
let departmentChart = null
let efficiencyChart = null
let inpatientChart = null
let surgeryTrendChart = null

// 格式化函数
const formatNumber = (num) => {
  if (num === undefined || num === null) return '--'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const formatGrowth = (value) => {
  if (value === undefined || value === null) return '--'
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${value}%`
}

const formatPercent = (value) => {
  if (value === undefined || value === null) return '--'
  return `${value}%`
}

const formatChange = (value) => {
  if (value === undefined || value === null) return '--'
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${value}天`
}

// 加载概况数据
const loadOverview = async () => {
  try {
    const res = await getServiceOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载服务概况失败:', error)
    overviewData.value = {
      totalVisits: 38500000,
      outpatientVisits: 35200000,
      emergencyVisits: 2800000,
      discharges: 1250000,
      surgeries: 850000,
      visitsGrowth: 4.2,
      dischargesGrowth: 3.5,
      outpatientRatio: 91.4,
      inpatientSurgeryRatio: 68,
      rescueSuccessRate: 98.5,
      averageHospitalStay: 9.2,
      stayChange: -0.3,
      bedTurnoverRate: 32.5,
      dailyVisitsPerDoctor: 8.5
    }
  }
}

// 加载门诊住院趋势图
const loadTrendChart = async () => {
  try {
    const res = await getServiceTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      outpatientVisits: [2850, 2980, 3120, 2980, 3150, 3280, 3420, 3520, 3620, 3720],
      inpatientVisits: [85.5, 88.2, 92.5, 88.5, 95.2, 102.5, 110.5, 118.2, 125.5, 132.5]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      years: ['2017', '2018', '9', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      outpatientVisits: [2850, 2980, 3120, 2980, 3150, 3280, 3420, 3520, 3620, 3720],
      inpatientVisits: [85.5, 88.2, 92.5, 88.5, 95.2, 102.5, 110.5, 118.2, 125.5, 132.5]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['门诊量（万人次）', '住院量（万人次）'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: [{ type: 'value', name: '门诊量（万人次）' }, { type: 'value', name: '住院量（万人次）' }],
      series: [
        { name: '门诊量（万人次）', type: 'line', data: data.outpatientVisits, smooth: true, lineStyle: { width: 3, color: '#409eff' }, symbol: 'circle' },
        { name: '住院量（万人次）', type: 'line', data: data.inpatientVisits, smooth: true, lineStyle: { width: 3, color: '#67c23a' }, symbol: 'diamond', yAxisIndex: 1 }
      ]
    })
  })
}

// 加载各月门诊量图
const loadMonthlyChart = async () => {
  try {
    const res = await getOutpatientStatistics({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      visits: [285, 265, 310, 325, 330, 340, 355, 360, 345, 335, 320, 305]
    }
    renderMonthlyChart(data)
  } catch (error) {
    renderMonthlyChart({
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      visits: [285, 265, 310, 325, 330, 340, 355, 360, 345, 335, 320, 305]
    })
  }
}

const renderMonthlyChart = (data) => {
  nextTick(() => {
    if (monthlyChart) monthlyChart.dispose()
    monthlyChart = echarts.init(monthlyChartRef.value)
    monthlyChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.months, name: '月份' },
      yAxis: { type: 'value', name: '门诊量（万人次）' },
      series: [{
        type: 'bar',
        data: data.visits,
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#5470c6' },
        label: { show: true, position: 'top', formatter: '{c}万' }
      }]
    })
  })
}

// 加载手术类型分布图
const loadSurgeryChart = async () => {
  try {
    const res = await getSurgeryStatistics({ year: queryParams.year })
    const data = res.data || [
      { name: '三级手术', value: 35.5 },
      { name: '四级手术', value: 18.2 },
      { name: '二级手术', value: 28.5 },
      { name: '一级手术', value: 17.8 }
    ]
    renderSurgeryChart(data)
  } catch (error) {
    renderSurgeryChart([
      { name: '三级手术', value: 35.5 },
      { name: '四级手术', value: 18.2 },
      { name: '二级手术', value: 28.5 },
      { name: '一级手术', value: 17.8 }
    ])
  }
}

const renderSurgeryChart = (data) => {
  nextTick(() => {
    if (surgeryChart) surgeryChart.dispose()
    surgeryChart = echarts.init(surgeryChartRef.value)
    surgeryChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        label: { formatter: '{b}: {d}%' },
        color: ['#f56c6c', '#e6a23c', '#409eff', '#67c23a']
      }]
    })
  })
}

// 加载科室排行图
const loadDepartmentChart = async () => {
  try {
    const res = await getDepartmentRanking({
      year: queryParams.year,
      limit: 10
    })
    const data = res.data || [
      { name: '内科', visits: 68.5 },
      { name: '外科', visits: 52.3 },
      { name: '儿科', visits: 45.2 },
      { name: '妇产科', visits: 42.5 },
      { name: '急诊科', visits: 38.5 },
      { name: '中医科', visits: 32.5 },
      { name: '眼科', visits: 28.5 },
      { name: '耳鼻喉科', visits: 25.5 },
      { name: '皮肤科', visits: 22.5 },
      { name: '口腔科', visits: 18.5 }
    ]
    renderDepartmentChart(data)
  } catch (error) {
    renderDepartmentChart([
      { name: '内科', visits: 68.5 },
      { name: '外科', visits: 52.3 },
      { name: '儿科', visits: 45.2 },
      { name: '妇产科', visits: 42.5 },
      { name: '急诊科', visits: 38.5 }
    ])
  }
}

const renderDepartmentChart = (data) => {
  nextTick(() => {
    if (departmentChart) departmentChart.dispose()
    departmentChart = echarts.init(departmentChartRef.value)
    departmentChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', name: '门诊量（万人次）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.visits),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#91cc75' },
        label: { show: true, position: 'top', formatter: '{c}万' }
      }]
    })
  })
}

// 加载服务效率指标图
const loadEfficiencyChart = async () => {
  try {
    const res = await getServiceEfficiency({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || [
      { name: '门诊医师日均诊疗', value: 8.5 },
      { name: '住院医师日均负担', value: 2.8 },
      { name: '床位周转次数', value: 32.5 },
      { name: '平均住院日', value: 9.2 }
    ]
    renderEfficiencyChart(data)
  } catch (error) {
    renderEfficiencyChart([
      { name: '门诊医师日均诊疗', value: 8.5 },
      { name: '住院医师日均负担', value: 2.8 },
      { name: '床位周转次数', value: 32.5 },
      { name: '平均住院日', value: 9.2 }
    ])
  }
}

const renderEfficiencyChart = (data) => {
  nextTick(() => {
    if (efficiencyChart) efficiencyChart.dispose()
    efficiencyChart = echarts.init(efficiencyChartRef.value)
    efficiencyChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 15, interval: 0 } },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => {
            const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a']
            return colors[params.dataIndex % colors.length]
          }
        },
        label: { show: true, position: 'top', formatter: (params) => {
            const val = params.value
            if (params.dataIndex === 3) return val + '天'
            return val
          } }
      }]
    })
  })
}

// 加载住院服务趋势图
const loadInpatientChart = async () => {
  try {
    const res = await getInpatientStatistics({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      discharges: [9.2, 8.5, 10.2, 10.5, 10.8, 11.2, 11.5, 11.2, 10.8, 10.5, 9.8, 9.5]
    }
    renderInpatientChart(data)
  } catch (error) {
    renderInpatientChart({
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      discharges: [9.2, 8.5, 10.2, 10.5, 10.8, 11.2, 11.5, 11.2, 10.8, 10.5, 9.8, 9.5]
    })
  }
}

const renderInpatientChart = (data) => {
  nextTick(() => {
    if (inpatientChart) inpatientChart.dispose()
    inpatientChart = echarts.init(inpatientChartRef.value)
    inpatientChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.months, name: '月份' },
      yAxis: { type: 'value', name: '出院人数（万人次）' },
      series: [{
        type: 'line',
        data: data.discharges,
        smooth: true,
        lineStyle: { width: 3, color: '#e6a23c' },
        areaStyle: { opacity: 0.3, color: '#e6a23c' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}万' }
      }]
    })
  })
}

// 加载手术量趋势图
const loadSurgeryTrendChart = async () => {
  try {
    const res = await getSurgeryStatistics({ year: queryParams.year })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      surgeries: [65.5, 68.5, 72.5, 68.5, 75.5, 78.5, 81.5, 84.5, 85.5, 87.5]
    }
    renderSurgeryTrendChart(data)
  } catch (error) {
    renderSurgeryTrendChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      surgeries: [65.5, 68.5, 72.5, 68.5, 75.5, 78.5, 81.5, 84.5, 85.5, 87.5]
    })
  }
}

const renderSurgeryTrendChart = (data) => {
  nextTick(() => {
    if (surgeryTrendChart) surgeryTrendChart.dispose()
    surgeryTrendChart = echarts.init(surgeryTrendChartRef.value)
    surgeryTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '手术量（万例）' },
      series: [{
        type: 'line',
        data: data.surgeries,
        smooth: true,
        lineStyle: { width: 3, color: '#f56c6c' },
        areaStyle: { opacity: 0.3, color: '#f56c6c' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}万' }
      }]
    })
  })
}

// 加载服务列表
const loadServiceList = async () => {
  loading.value = true
  try {
    const res = await getServiceList({
      year: queryParams.year,
      month: queryParams.month,
      region: queryParams.region.join(','),
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    serviceList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载服务列表失败:', error)
    serviceList.value = [
      { institutionName: '市人民医院', institutionType: '医院', outpatientVisits: 5200000, emergencyVisits: 420000, inpatientVisits: 185000, discharges: 182000, surgeries: 125000, averageHospitalStay: 8.5, bedTurnoverRate: 38.5 },
      { institutionName: '市中医院', institutionType: '医院', outpatientVisits: 2850000, emergencyVisits: 185000, inpatientVisits: 95000, discharges: 93000, surgeries: 52000, averageHospitalStay: 9.2, bedTurnoverRate: 35.2 },
      { institutionName: 'A区人民医院', institutionType: '医院', outpatientVisits: 1850000, emergencyVisits: 125000, inpatientVisits: 62000, discharges: 61000, surgeries: 38500, averageHospitalStay: 8.8, bedTurnoverRate: 36.5 }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  loadAllData()
}

// 重置
const resetQuery = () => {
  queryParams.year = new Date().getFullYear()
  queryParams.month = ''
  queryParams.region = []
  queryParams.pageNum = 1
  loadAllData()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadServiceList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadServiceList()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadTrendChart()
  loadMonthlyChart()
  loadSurgeryChart()
  loadDepartmentChart()
  loadEfficiencyChart()
  loadInpatientChart()
  loadSurgeryTrendChart()
  loadServiceList()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 监听月份变化
watch(() => queryParams.month, () => {
  loadServiceList()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.service-container {
  padding: 20px;
}

.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.indicator-row {
  margin-bottom: 20px;
}

.indicator-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.indicator-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.indicator-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.indicator-trend {
  font-size: 12px;
  color: #666;
}

.indicator-sub {
  font-size: 12px;
  color: #999;
}

.trend-up {
  color: #f56c6c;
  margin-left: 5px;
}

.trend-down {
  color: #67c23a;
  margin-left: 5px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  height: 300px;
}

.chart-container-large {
  height: 350px;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table-header {
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>