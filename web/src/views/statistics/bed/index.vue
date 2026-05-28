<template>
  <div class="bed-container">
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
        <el-form-item label="区域">
          <el-cascader
              v-model="queryParams.region"
              :options="regionOptions"
              :props="{ checkStrictly: true }"
              placeholder="请选择区域"
              clearable
          />
        </el-form-item>
        <el-form-item label="机构类型">
          <el-select v-model="queryParams.institutionType" placeholder="请选择机构类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="医院" value="hospital" />
            <el-option label="基层医疗卫生机构" value="primary" />
          </el-select>
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
            <div class="indicator-title">床位总数</div>
            <div class="indicator-value">{{ overviewData.totalBeds?.toLocaleString() || '--' }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.bedGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.bedGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">每千人口床位数</div>
            <div class="indicator-value">{{ overviewData.bedsPerThousand || '--' }}</div>
            <div class="indicator-sub">国家标准 6.5 张</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">医院床位数</div>
            <div class="indicator-value">{{ overviewData.hospitalBeds?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">占比 {{ formatPercent(overviewData.hospitalBedRatio) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">基层床位数</div>
            <div class="indicator-value">{{ overviewData.primaryBeds?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">占比 {{ formatPercent(overviewData.primaryBedRatio) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">床位使用率</div>
          <div class="indicator-value">{{ overviewData.bedUtilizationRate || '--' }}%</div>
          <div class="indicator-sub">
            <span :class="overviewData.bedUtilizationRate >= 85 ? 'trend-up' : 'trend-down'">
              {{ overviewData.bedUtilizationRate >= 85 ? '偏高' : '正常' }}
            </span>
          </div>
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
          <div class="indicator-title">ICU床位数</div>
          <div class="indicator-value">{{ overviewData.icuBeds?.toLocaleString() || '--' }}</div>
          <div class="indicator-sub">占床位比 {{ formatPercent(overviewData.icuBedRatio) }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 床位类型分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">床位类型分布</span>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 床位利用率趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">床位利用率趋势</span>
          </div>
          <div ref="utilizationChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 床位数量趋势 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">床位数量变化趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 区域床位分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">各区县床位分布</span>
          </div>
          <div ref="distributionChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 机构床位排行 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医疗机构床位排行（TOP10）</span>
          </div>
          <div ref="rankingChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 平均住院日趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">平均住院日变化趋势</span>
          </div>
          <div ref="stayChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 床位周转次数趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">床位周转次数变化趋势</span>
          </div>
          <div ref="turnoverChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 床位详情表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">床位详细信息</span>
      </div>
      <el-table :data="bedList" v-loading="loading" stripe>
        <el-table-column prop="institutionName" label="机构名称" min-width="200" />
        <el-table-column prop="institutionType" label="机构类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.institutionType === '医院' ? 'primary' : 'success'">
              {{ scope.row.institutionType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalBeds" label="总床位数" sortable width="100">
          <template #default="scope">
            {{ scope.row.totalBeds?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="generalBeds" label="普通床位" sortable width="100" />
        <el-table-column prop="icuBeds" label="ICU床位" sortable width="100" />
        <el-table-column prop="emergencyBeds" label="急诊床位" sortable width="100" />
        <el-table-column prop="obstetricBeds" label="产科床位" sortable width="100" />
        <el-table-column prop="utilizationRate" label="使用率" sortable width="100">
          <template #default="scope">
            <el-progress :percentage="scope.row.utilizationRate" :stroke-width="8" :show-text="false" />
            <span>{{ scope.row.utilizationRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="averageStay" label="平均住院日" sortable width="100" />
        <el-table-column prop="region" label="所在区域" width="100" />
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
  getBedOverview,
  getBedTypeDistribution,
  getBedUtilization,
  getBedTrend,
  getBedDistribution,
  getInstitutionBedRanking,
  getBedList
} from '@/api/statistics/bed'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  year: new Date().getFullYear(),
  region: [],
  institutionType: '',
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
const bedList = ref([])
const total = ref(0)

// 图表实例
const typeChartRef = ref(null)
const utilizationChartRef = ref(null)
const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const rankingChartRef = ref(null)
const stayChartRef = ref(null)
const turnoverChartRef = ref(null)
let typeChart = null
let utilizationChart = null
let trendChart = null
let distributionChart = null
let rankingChart = null
let stayChart = null
let turnoverChart = null

// 格式化函数
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
    const res = await getBedOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载床位概况失败:', error)
    overviewData.value = {
      totalBeds: 85000,
      hospitalBeds: 72000,
      primaryBeds: 13000,
      icuBeds: 3200,
      bedsPerThousand: 6.8,
      bedGrowth: 3.5,
      hospitalBedRatio: 84.7,
      primaryBedRatio: 15.3,
      icuBedRatio: 3.8,
      bedUtilizationRate: 87.5,
      averageHospitalStay: 9.2,
      stayChange: -0.3,
      bedTurnoverRate: 32.5
    }
  }
}

// 加载床位类型分布图
const loadTypeChart = async () => {
  try {
    const res = await getBedTypeDistribution({ year: queryParams.year })
    const data = res.data || [
      { name: '普通床位', value: 68500 },
      { name: 'ICU床位', value: 3200 },
      { name: '急诊床位', value: 1800 },
      { name: '产科床位', value: 2500 },
      { name: '儿科床位', value: 3500 },
      { name: '其他床位', value: 5500 }
    ]
    renderTypeChart(data)
  } catch (error) {
    renderTypeChart([
      { name: '普通床位', value: 68500 },
      { name: 'ICU床位', value: 3200 },
      { name: '急诊床位', value: 1800 },
      { name: '产科床位', value: 2500 },
      { name: '儿科床位', value: 3500 },
      { name: '其他床位', value: 5500 }
    ])
  }
}

const renderTypeChart = (data) => {
  nextTick(() => {
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', type: 'scroll' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        label: { formatter: '{b}: {d}%' }
      }]
    })
  })
}

// 加载床位利用率趋势图
const loadUtilizationChart = async () => {
  try {
    const res = await getBedUtilization({ year: queryParams.year })
    const data = res.data || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      utilizationRates: [85.2, 84.5, 86.1, 87.3, 88.5, 89.2, 90.1, 89.8, 88.5, 87.2, 86.5, 85.8]
    }
    renderUtilizationChart(data)
  } catch (error) {
    renderUtilizationChart({
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      utilizationRates: [85.2, 84.5, 86.1, 87.3, 88.5, 89.2, 90.1, 89.8, 88.5, 87.2, 86.5, 85.8]
    })
  }
}

const renderUtilizationChart = (data) => {
  nextTick(() => {
    if (utilizationChart) utilizationChart.dispose()
    utilizationChart = echarts.init(utilizationChartRef.value)
    utilizationChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.months, name: '月份' },
      yAxis: { type: 'value', name: '床位使用率（%）', min: 70, max: 100 },
      series: [{
        type: 'line',
        data: data.utilizationRates,
        smooth: true,
        lineStyle: { width: 3, color: '#e6a23c' },
        areaStyle: { opacity: 0.3, color: '#e6a23c' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}%' }
      }]
    })
  })
}

// 加载床位趋势图
const loadTrendChart = async () => {
  try {
    const res = await getBedTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      totalBeds: [65000, 68000, 71000, 73500, 76000, 78500, 81000, 83000, 84500, 86000],
      hospitalBeds: [55000, 57500, 60000, 62000, 64000, 66000, 68000, 69800, 71000, 72200],
      primaryBeds: [10000, 10500, 11000, 11500, 12000, 12500, 13000, 13200, 13500, 13800]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      totalBeds: [65000, 68000, 71000, 73500, 76000, 78500, 81000, 83000, 84500, 86000],
      hospitalBeds: [55000, 57500, 60000, 62000, 64000, 66000, 68000, 69800, 71000, 72200],
      primaryBeds: [10000, 10500, 11000, 11500, 12000, 12500, 13000, 13200, 13500, 13800]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['总床位数', '医院床位数', '基层床位数'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '床位数（张）' },
      series: [
        { name: '总床位数', type: 'line', data: data.totalBeds, smooth: true, lineStyle: { width: 3 }, symbol: 'circle' },
        { name: '医院床位数', type: 'line', data: data.hospitalBeds, smooth: true, lineStyle: { width: 2 }, symbol: 'diamond' },
        { name: '基层床位数', type: 'line', data: data.primaryBeds, smooth: true, lineStyle: { width: 2 }, symbol: 'triangle' }
      ]
    })
  })
}

// 加载区域分布图
const loadDistributionChart = async () => {
  try {
    const res = await getBedDistribution({ year: queryParams.year })
    const data = res.data || [
      { name: 'A区', beds: 18500, bedsPerThousand: 7.2 },
      { name: 'B区', beds: 16200, bedsPerThousand: 6.8 },
      { name: 'C区', beds: 13800, bedsPerThousand: 6.5 },
      { name: 'D区', beds: 11500, bedsPerThousand: 6.2 },
      { name: 'E区', beds: 9800, bedsPerThousand: 5.9 },
      { name: 'F区', beds: 7200, bedsPerThousand: 5.5 }
    ]
    renderDistributionChart(data)
  } catch (error) {
    renderDistributionChart([
      { name: 'A区', beds: 18500, bedsPerThousand: 7.2 },
      { name: 'B区', beds: 16200, bedsPerThousand: 6.8 },
      { name: 'C区', beds: 13800, bedsPerThousand: 6.5 },
      { name: 'D区', beds: 11500, bedsPerThousand: 6.2 },
      { name: 'E区', beds: 9800, bedsPerThousand: 5.9 },
      { name: 'F区', beds: 7200, bedsPerThousand: 5.5 }
    ])
  }
}

const renderDistributionChart = (data) => {
  nextTick(() => {
    if (distributionChart) distributionChart.dispose()
    distributionChart = echarts.init(distributionChartRef.value)
    distributionChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), name: '区域' },
      yAxis: { type: 'value', name: '床位数（张）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.beds),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#91cc75' },
        label: { show: true, position: 'top', formatter: '{c}张' }
      }]
    })
  })
}

// 加载机构床位排行图
const loadRankingChart = async () => {
  try {
    const res = await getInstitutionBedRanking({
      year: queryParams.year,
      limit: 10
    })
    const data = res.data || [
      { name: '市人民医院', beds: 3200, utilizationRate: 92.5 },
      { name: '市中医院', beds: 1800, utilizationRate: 88.2 },
      { name: '市中心医院', beds: 1500, utilizationRate: 85.6 },
      { name: '市妇幼保健院', beds: 800, utilizationRate: 82.3 },
      { name: '市第二人民医院', beds: 750, utilizationRate: 86.5 },
      { name: 'A区人民医院', beds: 650, utilizationRate: 84.2 },
      { name: 'B区人民医院', beds: 580, utilizationRate: 81.5 },
      { name: 'C区人民医院', beds: 480, utilizationRate: 79.8 },
      { name: '中西医结合医院', beds: 420, utilizationRate: 76.5 },
      { name: '市儿童医院', beds: 380, utilizationRate: 89.5 }
    ]
    renderRankingChart(data)
  } catch (error) {
    renderRankingChart([
      { name: '市人民医院', beds: 3200, utilizationRate: 92.5 },
      { name: '市中医院', beds: 1800, utilizationRate: 88.2 },
      { name: '市中心医院', beds: 1500, utilizationRate: 85.6 },
      { name: '市妇幼保健院', beds: 800, utilizationRate: 82.3 },
      { name: '市第二人民医院', beds: 750, utilizationRate: 86.5 }
    ])
  }
}

const renderRankingChart = (data) => {
  nextTick(() => {
    if (rankingChart) rankingChart.dispose()
    rankingChart = echarts.init(rankingChartRef.value)
    rankingChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', name: '床位数（张）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.beds),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => {
            const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#5470c6']
            return colors[params.dataIndex % colors.length]
          }
        },
        label: { show: true, position: 'top', formatter: '{c}张' }
      }]
    })
  })
}

// 加载平均住院日趋势图
const loadStayChart = async () => {
  try {
    const res = await getBedTrend({ year: queryParams.year })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      averageStay: [10.5, 10.2, 9.9, 9.6, 9.4, 9.3, 9.2, 9.2, 9.1, 9.0]
    }
    renderStayChart(data)
  } catch (error) {
    renderStayChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      averageStay: [10.5, 10.2, 9.9, 9.6, 9.4, 9.3, 9.2, 9.2, 9.1, 9.0]
    })
  }
}

const renderStayChart = (data) => {
  nextTick(() => {
    if (stayChart) stayChart.dispose()
    stayChart = echarts.init(stayChartRef.value)
    stayChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '平均住院日（天）' },
      series: [{
        type: 'line',
        data: data.averageStay,
        smooth: true,
        lineStyle: { width: 3, color: '#409eff' },
        areaStyle: { opacity: 0.3, color: '#409eff' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}天' }
      }]
    })
  })
}

// 加载床位周转次数趋势图
const loadTurnoverChart = async () => {
  try {
    const res = await getBedTrend({ year: queryParams.year })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      turnoverRate: [28.5, 29.2, 30.1, 31.2, 32.5, 33.8, 34.2, 34.5, 34.2, 33.8]
    }
    renderTurnoverChart(data)
  } catch (error) {
    renderTurnoverChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      turnoverRate: [28.5, 29.2, 30.1, 31.2, 32.5, 33.8, 34.2, 34.5, 34.2, 33.8]
    })
  }
}

const renderTurnoverChart = (data) => {
  nextTick(() => {
    if (turnoverChart) turnoverChart.dispose()
    turnoverChart = echarts.init(turnoverChartRef.value)
    turnoverChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '床位周转次数（次/年）' },
      series: [{
        type: 'line',
        data: data.turnoverRate,
        smooth: true,
        lineStyle: { width: 3, color: '#67c23a' },
        areaStyle: { opacity: 0.3, color: '#67c23a' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}次' }
      }]
    })
  })
}

// 加载床位列表
const loadBedList = async () => {
  loading.value = true
  try {
    const res = await getBedList({
      year: queryParams.year,
      region: queryParams.region.join(','),
      institutionType: queryParams.institutionType,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    bedList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载床位列表失败:', error)
    bedList.value = [
      { institutionName: '市人民医院', institutionType: '医院', totalBeds: 3200, generalBeds: 2800, icuBeds: 120, emergencyBeds: 80, obstetricBeds: 60, utilizationRate: 92.5, averageStay: 8.5, region: 'A区' },
      { institutionName: '市中医院', institutionType: '医院', totalBeds: 1800, generalBeds: 1600, icuBeds: 60, emergencyBeds: 50, obstetricBeds: 40, utilizationRate: 88.2, averageStay: 9.2, region: 'B区' },
      { institutionName: 'A区社区卫生服务中心', institutionType: '基层', totalBeds: 80, generalBeds: 80, icuBeds: 0, emergencyBeds: 0, obstetricBeds: 0, utilizationRate: 75.5, averageStay: 6.5, region: 'A区' }
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
  queryParams.region = []
  queryParams.institutionType = ''
  queryParams.pageNum = 1
  loadAllData()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadBedList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadBedList()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadTypeChart()
  loadUtilizationChart()
  loadTrendChart()
  loadDistributionChart()
  loadRankingChart()
  loadStayChart()
  loadTurnoverChart()
  loadBedList()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 监听机构类型变化
watch(() => queryParams.institutionType, () => {
  queryParams.pageNum = 1
  loadBedList()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.bed-container {
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

:deep(.el-progress) {
  width: 80px;
  margin-right: 8px;
  display: inline-block;
}
</style>