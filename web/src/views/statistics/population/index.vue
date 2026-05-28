<template>
  <div class="population-container">
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
            <div class="indicator-title">总人口</div>
            <div class="indicator-value">{{ overviewData.totalPopulation?.toLocaleString() || '--' }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.populationGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.populationGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">男性人口</div>
            <div class="indicator-value">{{ overviewData.malePopulation?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">占比 {{ formatPercent(overviewData.maleRatio) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">女性人口</div>
            <div class="indicator-value">{{ overviewData.femalePopulation?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">占比 {{ formatPercent(overviewData.femaleRatio) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">性别比</div>
            <div class="indicator-value">{{ overviewData.genderRatio || '--' }}</div>
            <div class="indicator-sub">(以女性=100)</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 年龄结构饼图 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">年龄结构</span>
          </div>
          <div ref="ageChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 性别比例饼图 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">性别比例</span>
          </div>
          <div ref="genderChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 人口趋势折线图 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">人口变化趋势</span>
            <el-radio-group v-model="trendType" size="small" @change="loadTrendChart">
              <el-radio-button label="history">历史数据</el-radio-button>
              <el-radio-button label="forecast">预测数据</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 地域分布表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">各区县人口统计</span>
      </div>
      <el-table :data="distributionData" v-loading="loading" stripe>
        <el-table-column prop="regionName" label="区域" width="180" />
        <el-table-column prop="totalPopulation" label="总人口" sortable>
          <template #default="scope">
            {{ scope.row.totalPopulation?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="malePopulation" label="男性" sortable>
          <template #default="scope">
            {{ scope.row.malePopulation?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="femalePopulation" label="女性" sortable>
          <template #default="scope">
            {{ scope.row.femalePopulation?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="urbanPopulation" label="城镇人口" sortable>
          <template #default="scope">
            {{ scope.row.urbanPopulation?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="ruralPopulation" label="乡村人口" sortable>
          <template #default="scope">
            {{ scope.row.ruralPopulation?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="populationDensity" label="人口密度(人/km²)" sortable />
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
  getPopulationOverview,
  getPopulationStructure,
  getPopulationTrend,
  getPopulationDistribution
} from '@/api/statistics/population'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  year: new Date().getFullYear(),
  region: [],
  pageNum: 1,
  pageSize: 10
})

// 年份选项（近10年）
const yearOptions = ref([])
for (let i = 2017; i <= new Date().getFullYear(); i++) {
  yearOptions.value.push(i)
}

// 区域选项（示例数据，实际从后端获取）
const regionOptions = ref([
  { value: 'city', label: '全市', children: [
      { value: 'district1', label: 'A区' },
      { value: 'district2', label: 'B区' },
      { value: 'district3', label: 'C区' }
    ]}
])

// 趋势图类型
const trendType = ref('history')

// 数据存储
const overviewData = ref({})
const distributionData = ref([])
const total = ref(0)

// 图表实例
const ageChartRef = ref(null)
const genderChartRef = ref(null)
const trendChartRef = ref(null)
let ageChart = null
let genderChart = null
let trendChart = null

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

// 加载概况数据
const loadOverview = async () => {
  try {
    const res = await getPopulationOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载概况数据失败:', error)
    // 使用模拟数据
    overviewData.value = {
      totalPopulation: 12500000,
      malePopulation: 6380000,
      femalePopulation: 6120000,
      populationGrowth: 1.2,
      maleRatio: 51.04,
      femaleRatio: 48.96,
      genderRatio: 104.25
    }
  }
}

// 加载年龄结构图表
const loadAgeChart = async () => {
  try {
    const res = await getPopulationStructure({
      type: 'age',
      year: queryParams.year
    })
    const data = res.data || [
      { name: '0-14岁', value: 15.2 },
      { name: '15-64岁', value: 72.5 },
      { name: '65岁及以上', value: 12.3 }
    ]
    renderAgeChart(data)
  } catch (error) {
    console.error('加载年龄结构失败:', error)
    renderAgeChart([
      { name: '0-14岁', value: 15.2 },
      { name: '15-64岁', value: 72.5 },
      { name: '65岁及以上', value: 12.3 }
    ])
  }
}

const renderAgeChart = (data) => {
  nextTick(() => {
    if (ageChart) {
      ageChart.dispose()
    }
    ageChart = echarts.init(ageChartRef.value)
    ageChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        emphasis: { scale: true },
        label: { formatter: '{b}: {d}%' }
      }]
    })
  })
}

// 加载性别比例图表
const loadGenderChart = async () => {
  try {
    const res = await getPopulationStructure({
      type: 'gender',
      year: queryParams.year
    })
    const data = res.data || [
      { name: '男性', value: overviewData.value.maleRatio || 51.04 },
      { name: '女性', value: overviewData.value.femaleRatio || 48.96 }
    ]
    renderGenderChart(data)
  } catch (error) {
    renderGenderChart([
      { name: '男性', value: 51.04 },
      { name: '女性', value: 48.96 }
    ])
  }
}

const renderGenderChart = (data) => {
  nextTick(() => {
    if (genderChart) {
      genderChart.dispose()
    }
    genderChart = echarts.init(genderChartRef.value)
    genderChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        emphasis: { scale: true },
        label: { formatter: '{b}: {d}%' },
        color: ['#5470c6', '#fac858']
      }]
    })
  })
}

// 加载趋势图表
const loadTrendChart = async () => {
  try {
    const params = {
      year: queryParams.year,
      region: queryParams.region.join(','),
      type: trendType.value
    }
    const res = await getPopulationTrend(params)
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      values: [1180, 1195, 1210, 1220, 1235, 1240, 1248, 1255, 1262, 1270]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      values: [1180, 1195, 1210, 1220, 1235, 1240, 1248, 1255, 1262, 1270]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) {
      trendChart.dispose()
    }
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '人口（万人）' },
      series: [{
        data: data.values,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { width: 3 },
        symbol: 'circle',
        symbolSize: 8
      }]
    })
  })
}

// 加载地域分布数据
const loadDistribution = async () => {
  loading.value = true
  try {
    const res = await getPopulationDistribution({
      year: queryParams.year,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    distributionData.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载地域分布失败:', error)
    distributionData.value = [
      { regionName: 'A区', totalPopulation: 3200000, malePopulation: 1632000, femalePopulation: 1568000, urbanPopulation: 2800000, ruralPopulation: 400000, populationDensity: 1250 },
      { regionName: 'B区', totalPopulation: 2800000, malePopulation: 1428000, femalePopulation: 1372000, urbanPopulation: 2300000, ruralPopulation: 500000, populationDensity: 980 },
      { regionName: 'C区', totalPopulation: 1800000, malePopulation: 918000, femalePopulation: 882000, urbanPopulation: 1200000, ruralPopulation: 600000, populationDensity: 560 }
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
  queryParams.pageNum = 1
  loadAllData()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadDistribution()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadDistribution()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadAgeChart()
  loadGenderChart()
  loadTrendChart()
  loadDistribution()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.population-container {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  height: 400px;
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