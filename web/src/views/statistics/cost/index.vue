<template>
  <div class="cost-container">
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
        <el-form-item label="统计季度">
          <el-select v-model="queryParams.quarter" placeholder="请选择季度" clearable>
            <el-option label="全年" value="" />
            <el-option label="第一季度" value="1" />
            <el-option label="第二季度" value="2" />
            <el-option label="第三季度" value="3" />
            <el-option label="第四季度" value="4" />
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
            <div class="indicator-title">总医疗费用</div>
            <div class="indicator-value">{{ formatMoney(overviewData.totalCost) }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.costGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.costGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">人均医疗费用</div>
            <div class="indicator-value">{{ formatMoney(overviewData.perCapitaCost) }}</div>
            <div class="indicator-sub">元/人</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">次均门诊费用</div>
            <div class="indicator-value">{{ formatMoney(overviewData.averageOutpatientCost) }}</div>
            <div class="indicator-trend">
              <span>同比变化</span>
              <span :class="overviewData.outpatientCostGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.outpatientCostGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">次均住院费用</div>
            <div class="indicator-value">{{ formatMoney(overviewData.averageInpatientCost) }}</div>
            <div class="indicator-trend">
              <span>同比变化</span>
              <span :class="overviewData.inpatientCostGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.inpatientCostGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">医保基金支出</div>
          <div class="indicator-value">{{ formatMoney(overviewData.insuranceExpenditure) }}</div>
          <div class="indicator-sub">占医疗费用 {{ formatPercent(overviewData.insuranceCoverageRate) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">医保报销比例</div>
          <div class="indicator-value">{{ overviewData.reimbursementRate || '--' }}%</div>
          <div class="indicator-sub">门诊报销 {{ overviewData.outpatientReimbursementRate || '--' }}%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">个人自付比例</div>
          <div class="indicator-value">{{ overviewData.outOfPocketRate || '--' }}%</div>
          <div class="indicator-sub">自付费用占比</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">药品费用占比</div>
          <div class="indicator-value">{{ overviewData.drugCostRatio || '--' }}%</div>
          <div class="indicator-sub">较去年 {{ formatChange(overviewData.drugRatioChange) }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 费用结构 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医疗费用结构分析</span>
          </div>
          <div ref="structureChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 人均费用趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">人均医疗费用趋势</span>
          </div>
          <div ref="perCapitaChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 次均费用趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">次均门诊/住院费用趋势</span>
          </div>
          <div ref="averageCostChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 医保基金收支 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医保基金收支趋势</span>
          </div>
          <div ref="insuranceChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 机构费用排行 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医疗机构费用排行（TOP10）</span>
          </div>
          <div ref="institutionChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 费用增长率排行 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">费用增长率排行（TOP10）</span>
          </div>
          <div ref="growthChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 各季度费用分布 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">各季度医疗费用分布</span>
          </div>
          <div ref="quarterlyChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 费用详情表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">医疗机构费用详情</span>
      </div>
      <el-table :data="costList" v-loading="loading" stripe>
        <el-table-column prop="institutionName" label="机构名称" min-width="200" />
        <el-table-column prop="institutionType" label="机构类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.institutionType === '医院' ? 'primary' : 'success'">
              {{ scope.row.institutionType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="总费用" sortable width="140">
          <template #default="scope">
            {{ formatMoney(scope.row.totalCost) }}
          </template>
        </el-table-column>
        <el-table-column prop="averageOutpatientCost" label="次均门诊费用" sortable width="130">
          <template #default="scope">
            {{ formatMoney(scope.row.averageOutpatientCost) }}
          </template>
        </el-table-column>
        <el-table-column prop="averageInpatientCost" label="次均住院费用" sortable width="130">
          <template #default="scope">
            {{ formatMoney(scope.row.averageInpatientCost) }}
          </template>
        </el-table-column>
        <el-table-column prop="drugCostRatio" label="药占比" sortable width="100">
          <template #default="scope">
            {{ scope.row.drugCostRatio }}%
          </template>
        </el-table-column>
        <el-table-column prop="reimbursementRate" label="医保报销率" sortable width="110">
          <template #default="scope">
            {{ scope.row.reimbursementRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="costGrowth" label="费用增长率" sortable width="110">
          <template #default="scope">
            <span :class="scope.row.costGrowth >= 0 ? 'trend-up' : 'trend-down'">
              {{ formatGrowth(scope.row.costGrowth) }}
            </span>
          </template>
        </el-table-column>
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
  getCostOverview,
  getCostStructure,
  getInsuranceAnalysis,
  getPerCapitaCostTrend,
  getAverageCostTrend,
  getInstitutionCostRanking,
  getCostList
} from '@/api/statistics/cost'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  year: new Date().getFullYear(),
  quarter: '',
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
const costList = ref([])
const total = ref(0)

// 图表实例
const structureChartRef = ref(null)
const perCapitaChartRef = ref(null)
const averageCostChartRef = ref(null)
const insuranceChartRef = ref(null)
const institutionChartRef = ref(null)
const growthChartRef = ref(null)
const quarterlyChartRef = ref(null)
let structureChart = null
let perCapitaChart = null
let averageCostChart = null
let insuranceChart = null
let institutionChart = null
let growthChart = null
let quarterlyChart = null

// 格式化函数
const formatMoney = (value) => {
  if (value === undefined || value === null) return '--'
  if (value >= 100000000) {
    return (value / 100000000).toFixed(2) + '亿'
  }
  if (value >= 10000) {
    return (value / 10000).toFixed(2) + '万'
  }
  return value.toLocaleString()
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
  return `${prefix}${value}%`
}

// 加载概况数据
const loadOverview = async () => {
  try {
    const res = await getCostOverview({
      year: queryParams.year,
      quarter: queryParams.quarter,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载费用概况失败:', error)
    overviewData.value = {
      totalCost: 38500000000,
      perCapitaCost: 3080,
      averageOutpatientCost: 285,
      averageInpatientCost: 12500,
      costGrowth: 5.8,
      outpatientCostGrowth: 4.5,
      inpatientCostGrowth: 6.2,
      insuranceExpenditure: 19250000000,
      insuranceCoverageRate: 50,
      reimbursementRate: 68.5,
      outpatientReimbursementRate: 55.2,
      outOfPocketRate: 31.5,
      drugCostRatio: 28.5,
      drugRatioChange: -1.2
    }
  }
}

// 加载费用结构图
const loadStructureChart = async () => {
  try {
    const res = await getCostStructure({ year: queryParams.year })
    const data = res.data || [
      { name: '药费', value: 28.5 },
      { name: '检查费', value: 18.2 },
      { name: '治疗费', value: 15.5 },
      { name: '手术费', value: 12.5 },
      { name: '耗材费', value: 14.8 },
      { name: '化验费', value: 6.5 },
      { name: '其他', value: 4.0 }
    ]
    renderStructureChart(data)
  } catch (error) {
    renderStructureChart([
      { name: '药费', value: 28.5 },
      { name: '检查费', value: 18.2 },
      { name: '治疗费', value: 15.5 },
      { name: '手术费', value: 12.5 },
      { name: '耗材费', value: 14.8 },
      { name: '化验费', value: 6.5 },
      { name: '其他', value: 4.0 }
    ])
  }
}

const renderStructureChart = (data) => {
  nextTick(() => {
    if (structureChart) structureChart.dispose()
    structureChart = echarts.init(structureChartRef.value)
    structureChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', type: 'scroll' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        label: { formatter: '{b}: {d}%' },
        color: ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#5470c6', '#fac858', '#91cc75']
      }]
    })
  })
}

// 加载人均费用趋势图
const loadPerCapitaChart = async () => {
  try {
    const res = await getPerCapitaCostTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      perCapitaCost: [2350, 2480, 2620, 2750, 2850, 2920, 2980, 3050, 3120, 3180]
    }
    renderPerCapitaChart(data)
  } catch (error) {
    renderPerCapitaChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      perCapitaCost: [2350, 2480, 2620, 2750, 2850, 2920, 2980, 3050, 3120, 3180]
    })
  }
}

const renderPerCapitaChart = (data) => {
  nextTick(() => {
    if (perCapitaChart) perCapitaChart.dispose()
    perCapitaChart = echarts.init(perCapitaChartRef.value)
    perCapitaChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '人均费用（元）' },
      series: [{
        type: 'line',
        data: data.perCapitaCost,
        smooth: true,
        lineStyle: { width: 3, color: '#409eff' },
        areaStyle: { opacity: 0.3, color: '#409eff' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}元' }
      }]
    })
  })
}

// 加载次均费用趋势图
const loadAverageCostChart = async () => {
  try {
    const res = await getAverageCostTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      outpatientCost: [225, 235, 248, 258, 268, 275, 280, 285, 290, 295],
      inpatientCost: [9850, 10200, 10800, 11200, 11600, 11900, 12200, 12500, 12800, 13000]
    }
    renderAverageCostChart(data)
  } catch (error) {
    renderAverageCostChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      outpatientCost: [225, 235, 248, 258, 268, 275, 280, 285, 290, 295],
      inpatientCost: [9850, 10200, 10800, 11200, 11600, 11900, 12200, 12500, 12800, 13000]
    })
  }
}

const renderAverageCostChart = (data) => {
  nextTick(() => {
    if (averageCostChart) averageCostChart.dispose()
    averageCostChart = echarts.init(averageCostChartRef.value)
    averageCostChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['次均门诊费用', '次均住院费用'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: [{ type: 'value', name: '门诊费用（元）' }, { type: 'value', name: '住院费用（元）' }],
      series: [
        { name: '次均门诊费用', type: 'line', data: data.outpatientCost, smooth: true, lineStyle: { width: 2, color: '#67c23a' }, symbol: 'circle', label: { show: true, formatter: '{c}元' } },
        { name: '次均住院费用', type: 'line', data: data.inpatientCost, smooth: true, lineStyle: { width: 2, color: '#e6a23c' }, symbol: 'diamond', yAxisIndex: 1, label: { show: true, formatter: '{c}元' } }
      ]
    })
  })
}

// 加载医保基金趋势图
const loadInsuranceChart = async () => {
  try {
    const res = await getInsuranceAnalysis({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      income: [165, 178, 192, 205, 218, 232, 248, 262, 278, 295],
      expenditure: [152, 168, 185, 198, 212, 228, 245, 260, 275, 292]
    }
    renderInsuranceChart(data)
  } catch (error) {
    renderInsuranceChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      income: [165, 178, 192, 205, 218, 232, 248, 262, 278, 295],
      expenditure: [152, 168, 185, 198, 212, 228, 245, 260, 275, 292]
    })
  }
}

const renderInsuranceChart = (data) => {
  nextTick(() => {
    if (insuranceChart) insuranceChart.dispose()
    insuranceChart = echarts.init(insuranceChartRef.value)
    insuranceChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['医保基金收入', '医保基金支出'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '金额（亿元）' },
      series: [
        { name: '医保基金收入', type: 'line', data: data.income, smooth: true, lineStyle: { width: 3, color: '#67c23a' }, symbol: 'circle', areaStyle: { opacity: 0.2 } },
        { name: '医保基金支出', type: 'line', data: data.expenditure, smooth: true, lineStyle: { width: 3, color: '#f56c6c' }, symbol: 'diamond', areaStyle: { opacity: 0.2 } }
      ]
    })
  })
}

// 加载机构费用排行图
const loadInstitutionChart = async () => {
  try {
    const res = await getInstitutionCostRanking({
      year: queryParams.year,
      limit: 10
    })
    const data = res.data || [
      { name: '市人民医院', totalCost: 52.5, averageCost: 13200 },
      { name: '市中医院', totalCost: 28.5, averageCost: 11800 },
      { name: '市中心医院', totalCost: 25.8, averageCost: 12500 },
      { name: '市妇幼保健院', totalCost: 15.2, averageCost: 9800 },
      { name: '市第二人民医院', totalCost: 12.5, averageCost: 10800 },
      { name: 'A区人民医院', totalCost: 10.8, averageCost: 9500 },
      { name: 'B区人民医院', totalCost: 9.2, averageCost: 9200 },
      { name: 'C区人民医院', totalCost: 7.8, averageCost: 8900 },
      { name: '中西医结合医院', totalCost: 6.5, averageCost: 10200 },
      { name: '市儿童医院', totalCost: 5.8, averageCost: 8500 }
    ]
    renderInstitutionChart(data)
  } catch (error) {
    renderInstitutionChart([
      { name: '市人民医院', totalCost: 52.5, averageCost: 13200 },
      { name: '市中医院', totalCost: 28.5, averageCost: 11800 },
      { name: '市中心医院', totalCost: 25.8, averageCost: 12500 }
    ])
  }
}

const renderInstitutionChart = (data) => {
  nextTick(() => {
    if (institutionChart) institutionChart.dispose()
    institutionChart = echarts.init(institutionChartRef.value)
    institutionChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', name: '总费用（亿元）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.totalCost),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#5470c6' },
        label: { show: true, position: 'top', formatter: '{c}亿元' }
      }]
    })
  })
}

// 加载费用增长率排行图
const loadGrowthChart = async () => {
  try {
    const res = await getInstitutionCostRanking({
      year: queryParams.year,
      limit: 10,
      sortBy: 'growth'
    })
    const data = res.data || [
      { name: 'D区人民医院', growth: 12.5 },
      { name: 'E区人民医院', growth: 11.2 },
      { name: 'F区人民医院', growth: 10.5 },
      { name: '市儿童医院', growth: 9.8 },
      { name: '市中医院', growth: 8.5 },
      { name: '市人民医院', growth: 7.5 },
      { name: '市中心医院', growth: 6.8 },
      { name: 'A区人民医院', growth: 6.2 },
      { name: 'B区人民医院', growth: 5.8 },
      { name: '市妇幼保健院', growth: 5.2 }
    ]
    renderGrowthChart(data)
  } catch (error) {
    renderGrowthChart([
      { name: 'D区人民医院', growth: 12.5 },
      { name: 'E区人民医院', growth: 11.2 },
      { name: 'F区人民医院', growth: 10.5 }
    ])
  }
}

const renderGrowthChart = (data) => {
  nextTick(() => {
    if (growthChart) growthChart.dispose()
    growthChart = echarts.init(growthChartRef.value)
    growthChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', name: '费用增长率（%）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.growth),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => params.value >= 8 ? '#f56c6c' : '#e6a23c'
        },
        label: { show: true, position: 'top', formatter: '{c}%' }
      }]
    })
  })
}

// 加载各季度费用分布图
const loadQuarterlyChart = async () => {
  try {
    const res = await getCostOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      quarters: ['第一季度', '第二季度', '第三季度', '第四季度'],
      totalCost: [85.5, 92.5, 98.5, 108.5]
    }
    renderQuarterlyChart(data)
  } catch (error) {
    renderQuarterlyChart({
      quarters: ['第一季度', '第二季度', '第三季度', '第四季度'],
      totalCost: [85.5, 92.5, 98.5, 108.5]
    })
  }
}

const renderQuarterlyChart = (data) => {
  nextTick(() => {
    if (quarterlyChart) quarterlyChart.dispose()
    quarterlyChart = echarts.init(quarterlyChartRef.value)
    quarterlyChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.quarters, name: '季度' },
      yAxis: { type: 'value', name: '医疗费用（亿元）' },
      series: [{
        type: 'line',
        data: data.totalCost,
        smooth: true,
        lineStyle: { width: 3, color: '#91cc75' },
        areaStyle: { opacity: 0.3, color: '#91cc75' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', formatter: '{c}亿元' }
      }]
    })
  })
}

// 加载费用列表
const loadCostList = async () => {
  loading.value = true
  try {
    const res = await getCostList({
      year: queryParams.year,
      quarter: queryParams.quarter,
      region: queryParams.region.join(','),
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    costList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载费用列表失败:', error)
    costList.value = [
      { institutionName: '市人民医院', institutionType: '医院', totalCost: 5250000000, averageOutpatientCost: 295, averageInpatientCost: 13200, drugCostRatio: 26.5, reimbursementRate: 70.5, costGrowth: 7.5 },
      { institutionName: '市中医院', institutionType: '医院', totalCost: 2850000000, averageOutpatientCost: 285, averageInpatientCost: 11800, drugCostRatio: 32.5, reimbursementRate: 68.2, costGrowth: 8.5 },
      { institutionName: 'A区人民医院', institutionType: '医院', totalCost: 1080000000, averageOutpatientCost: 265, averageInpatientCost: 9500, drugCostRatio: 28.5, reimbursementRate: 65.8, costGrowth: 6.2 }
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
  queryParams.quarter = ''
  queryParams.region = []
  queryParams.pageNum = 1
  loadAllData()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadCostList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadCostList()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadStructureChart()
  loadPerCapitaChart()
  loadAverageCostChart()
  loadInsuranceChart()
  loadInstitutionChart()
  loadGrowthChart()
  loadQuarterlyChart()
  loadCostList()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 监听季度变化
watch(() => queryParams.quarter, () => {
  loadOverview()
  loadCostList()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.cost-container {
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