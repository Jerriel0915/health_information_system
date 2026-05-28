<template>
  <div class="staff-container">
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
        <el-form-item label="人员类型">
          <el-select v-model="queryParams.staffType" placeholder="请选择人员类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="执业医师" value="doctor" />
            <el-option label="注册护士" value="nurse" />
            <el-option label="药师" value="pharmacist" />
            <el-option label="技师" value="technician" />
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
            <div class="indicator-title">卫生人员总数</div>
            <div class="indicator-value">{{ overviewData.totalStaff?.toLocaleString() || '--' }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.staffGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.staffGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">执业医师</div>
            <div class="indicator-value">{{ overviewData.doctorCount?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">每千人口医师数 {{ overviewData.doctorsPerThousand || '--' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">注册护士</div>
            <div class="indicator-value">{{ overviewData.nurseCount?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">每千人口护士数 {{ overviewData.nursesPerThousand || '--' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">医护比</div>
            <div class="indicator-value">{{ overviewData.doctorNurseRatio || '--' }}</div>
            <div class="indicator-sub">国家标准 1:1.25</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">药师</div>
          <div class="indicator-value">{{ overviewData.pharmacistCount?.toLocaleString() || '--' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">技师</div>
          <div class="indicator-value">{{ overviewData.technicianCount?.toLocaleString() || '--' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">管理人员</div>
          <div class="indicator-value">{{ overviewData.managerCount?.toLocaleString() || '--' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="indicator-title">工勤人员</div>
          <div class="indicator-value">{{ overviewData.supportCount?.toLocaleString() || '--' }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 人员类型分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">人员类型分布</span>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 职称结构 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">职称结构</span>
          </div>
          <div ref="titleChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 学历结构 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">学历结构</span>
          </div>
          <div ref="educationChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 医护比趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医护比趋势</span>
          </div>
          <div ref="ratioChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 人员趋势 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">卫生人员数量变化趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 区域分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">各区县人员分布</span>
          </div>
          <div ref="distributionChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 机构人员排行 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医疗机构人员排行（TOP10）</span>
          </div>
          <div ref="institutionChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 人员详情表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">卫生人员详细信息</span>
      </div>
      <el-table :data="staffList" v-loading="loading" stripe>
        <el-table-column prop="staffName" label="姓名" width="100" />
        <el-table-column prop="institutionName" label="所在机构" min-width="180" />
        <el-table-column prop="staffType" label="人员类型" width="100">
          <template #default="scope">
            <el-tag :type="getStaffTypeTagType(scope.row.staffType)">
              {{ scope.row.staffType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.title" :type="getTitleTagType(scope.row.title)">
              {{ scope.row.title }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="80" />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="yearsOfService" label="从业年限" width="80" />
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
  getStaffOverview,
  getStaffTitleStructure,
  getStaffEducationStructure,
  getStaffTypeDistribution,
  getStaffTrend,
  getDoctorNurseRatio,
  getStaffDistribution,
  getStaffList
} from '@/api/statistics/staff'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  year: new Date().getFullYear(),
  region: [],
  staffType: '',
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
const staffList = ref([])
const total = ref(0)

// 图表实例
const typeChartRef = ref(null)
const titleChartRef = ref(null)
const educationChartRef = ref(null)
const ratioChartRef = ref(null)
const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const institutionChartRef = ref(null)
let typeChart = null
let titleChart = null
let educationChart = null
let ratioChart = null
let trendChart = null
let distributionChart = null
let institutionChart = null

// 格式化函数
const formatGrowth = (value) => {
  if (value === undefined || value === null) return '--'
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${value}%`
}

const getStaffTypeTagType = (type) => {
  const map = {
    '执业医师': 'primary',
    '注册护士': 'success',
    '药师': 'warning',
    '技师': 'info',
    '管理人员': 'danger',
    '工勤人员': ''
  }
  return map[type] || 'info'
}

const getTitleTagType = (title) => {
  if (title === '正高级') return 'danger'
  if (title === '副高级') return 'warning'
  if (title === '中级') return 'primary'
  if (title === '初级') return 'success'
  return 'info'
}

// 加载概况数据
const loadOverview = async () => {
  try {
    const res = await getStaffOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载人员概况失败:', error)
    overviewData.value = {
      totalStaff: 125000,
      doctorCount: 38500,
      nurseCount: 52000,
      pharmacistCount: 8500,
      technicianCount: 7800,
      managerCount: 5200,
      supportCount: 13000,
      staffGrowth: 3.2,
      doctorsPerThousand: 3.08,
      nursesPerThousand: 4.16,
      doctorNurseRatio: '1:1.35'
    }
  }
}

// 加载人员类型分布图
const loadTypeChart = async () => {
  try {
    const res = await getStaffTypeDistribution({ year: queryParams.year })
    const data = res.data || [
      { name: '执业医师', value: 38500 },
      { name: '注册护士', value: 52000 },
      { name: '药师', value: 8500 },
      { name: '技师', value: 7800 },
      { name: '管理人员', value: 5200 },
      { name: '工勤人员', value: 13000 }
    ]
    renderTypeChart(data)
  } catch (error) {
    renderTypeChart([
      { name: '执业医师', value: 38500 },
      { name: '注册护士', value: 52000 },
      { name: '药师', value: 8500 },
      { name: '技师', value: 7800 },
      { name: '管理人员', value: 5200 },
      { name: '工勤人员', value: 13000 }
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

// 加载职称结构图
const loadTitleChart = async () => {
  try {
    const res = await getStaffTitleStructure({ year: queryParams.year })
    const data = res.data || [
      { name: '正高级', value: 3200 },
      { name: '副高级', value: 12500 },
      { name: '中级', value: 38500 },
      { name: '初级', value: 52800 },
      { name: '无职称', value: 18000 }
    ]
    renderTitleChart(data)
  } catch (error) {
    renderTitleChart([
      { name: '正高级', value: 3200 },
      { name: '副高级', value: 12500 },
      { name: '中级', value: 38500 },
      { name: '初级', value: 52800 },
      { name: '无职称', value: 18000 }
    ])
  }
}

const renderTitleChart = (data) => {
  nextTick(() => {
    if (titleChart) titleChart.dispose()
    titleChart = echarts.init(titleChartRef.value)
    titleChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), name: '职称' },
      yAxis: { type: 'value', name: '人数' },
      series: [{
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => {
            const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#909399']
            return colors[params.dataIndex] || '#409eff'
          }
        },
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载学历结构图
const loadEducationChart = async () => {
  try {
    const res = await getStaffEducationStructure({ year: queryParams.year })
    const data = res.data || [
      { name: '研究生', value: 8500 },
      { name: '本科', value: 52000 },
      { name: '大专', value: 42500 },
      { name: '中专及以下', value: 22000 }
    ]
    renderEducationChart(data)
  } catch (error) {
    renderEducationChart([
      { name: '研究生', value: 8500 },
      { name: '本科', value: 52000 },
      { name: '大专', value: 42500 },
      { name: '中专及以下', value: 22000 }
    ])
  }
}

const renderEducationChart = (data) => {
  nextTick(() => {
    if (educationChart) educationChart.dispose()
    educationChart = echarts.init(educationChartRef.value)
    educationChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        label: { formatter: '{b}: {d}%' }
      }]
    })
  })
}

// 加载医护比趋势图
const loadRatioChart = async () => {
  try {
    const res = await getDoctorNurseRatio({ year: queryParams.year })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      doctorCount: [28500, 29800, 31200, 32800, 34500, 35800, 36800, 37800, 38500, 39200],
      nurseCount: [36800, 38500, 40500, 42500, 44800, 46800, 48800, 50500, 52000, 53800],
      ratio: [1.29, 1.29, 1.30, 1.30, 1.30, 1.31, 1.33, 1.34, 1.35, 1.37]
    }
    renderRatioChart(data)
  } catch (error) {
    renderRatioChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      doctorCount: [28500, 29800, 31200, 32800, 34500, 35800, 36800, 37800, 38500, 39200],
      nurseCount: [36800, 38500, 40500, 42500, 44800, 46800, 48800, 50500, 52000, 53800],
      ratio: [1.29, 1.29, 1.30, 1.30, 1.30, 1.31, 1.33, 1.34, 1.35, 1.37]
    })
  }
}

const renderRatioChart = (data) => {
  nextTick(() => {
    if (ratioChart) ratioChart.dispose()
    ratioChart = echarts.init(ratioChartRef.value)
    ratioChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '医护比（1:？）' },
      series: [{
        name: '医护比',
        type: 'line',
        data: data.ratio,
        smooth: true,
        lineStyle: { width: 3, color: '#e6a23c' },
        areaStyle: { opacity: 0.3, color: '#e6a23c' },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载人员趋势图
const loadTrendChart = async () => {
  try {
    const res = await getStaffTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      doctorCount: [28500, 29800, 31200, 32800, 34500, 35800, 36800, 37800, 38500, 39200],
      nurseCount: [36800, 38500, 40500, 42500, 44800, 46800, 48800, 50500, 52000, 53800],
      pharmacistCount: [6200, 6500, 6800, 7100, 7400, 7700, 8000, 8200, 8500, 8700],
      technicianCount: [5600, 5800, 6100, 6400, 6700, 7000, 7300, 7500, 7800, 8000]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      doctorCount: [28500, 29800, 31200, 32800, 34500, 35800, 36800, 37800, 38500, 39200],
      nurseCount: [36800, 38500, 40500, 42500, 44800, 46800, 48800, 50500, 52000, 53800],
      pharmacistCount: [6200, 6500, 6800, 7100, 7400, 7700, 8000, 8200, 8500, 8700],
      technicianCount: [5600, 5800, 6100, 6400, 6700, 7000, 7300, 7500, 7800, 8000]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['执业医师', '注册护士', '药师', '技师'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '人数' },
      series: [
        { name: '执业医师', type: 'line', data: data.doctorCount, smooth: true, lineStyle: { width: 2 } },
        { name: '注册护士', type: 'line', data: data.nurseCount, smooth: true, lineStyle: { width: 2 } },
        { name: '药师', type: 'line', data: data.pharmacistCount, smooth: true, lineStyle: { width: 2 } },
        { name: '技师', type: 'line', data: data.technicianCount, smooth: true, lineStyle: { width: 2 } }
      ]
    })
  })
}

// 加载区域分布图
const loadDistributionChart = async () => {
  try {
    const res = await getStaffDistribution({ year: queryParams.year })
    const data = res.data || [
      { name: 'A区', doctors: 8500, nurses: 11500, total: 28500 },
      { name: 'B区', doctors: 7200, nurses: 9800, total: 24200 },
      { name: 'C区', doctors: 5800, nurses: 7800, total: 19500 },
      { name: 'D区', doctors: 4900, nurses: 6600, total: 16500 },
      { name: 'E区', doctors: 4200, nurses: 5600, total: 14000 },
      { name: 'F区', doctors: 3800, nurses: 5100, total: 12800 }
    ]
    renderDistributionChart(data)
  } catch (error) {
    renderDistributionChart([
      { name: 'A区', doctors: 8500, nurses: 11500, total: 28500 },
      { name: 'B区', doctors: 7200, nurses: 9800, total: 24200 },
      { name: 'C区', doctors: 5800, nurses: 7800, total: 19500 },
      { name: 'D区', doctors: 4900, nurses: 6600, total: 16500 },
      { name: 'E区', doctors: 4200, nurses: 5600, total: 14000 },
      { name: 'F区', doctors: 3800, nurses: 5100, total: 12800 }
    ])
  }
}

const renderDistributionChart = (data) => {
  nextTick(() => {
    if (distributionChart) distributionChart.dispose()
    distributionChart = echarts.init(distributionChartRef.value)
    distributionChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['执业医师', '注册护士'] },
      xAxis: { type: 'category', data: data.map(d => d.name), name: '区域' },
      yAxis: { type: 'value', name: '人数' },
      series: [
        { name: '执业医师', type: 'bar', data: data.map(d => d.doctors), itemStyle: { color: '#5470c6' }, label: { show: true, position: 'top' } },
        { name: '注册护士', type: 'bar', data: data.map(d => d.nurses), itemStyle: { color: '#fac858' }, label: { show: true, position: 'top' } }
      ]
    })
  })
}

// 加载机构人员排行图
const loadInstitutionChart = async () => {
  try {
    const res = await getStaffList({
      year: queryParams.year,
      pageNum: 1,
      pageSize: 10,
      sortBy: 'staffCount',
      order: 'desc'
    })
    const data = res.rows || [
      { name: '市人民医院', staffCount: 4500 },
      { name: '市中医院', staffCount: 2200 },
      { name: '市中心医院', staffCount: 1900 },
      { name: '市妇幼保健院', staffCount: 1100 },
      { name: '市第二人民医院', staffCount: 980 },
      { name: 'A区人民医院', staffCount: 850 },
      { name: 'B区人民医院', staffCount: 720 },
      { name: '市疾控中心', staffCount: 680 },
      { name: 'C区人民医院', staffCount: 580 },
      { name: '市卫生监督所', staffCount: 450 }
    ]
    renderInstitutionChart(data)
  } catch (error) {
    renderInstitutionChart([
      { name: '市人民医院', staffCount: 4500 },
      { name: '市中医院', staffCount: 2200 },
      { name: '市中心医院', staffCount: 1900 },
      { name: '市妇幼保健院', staffCount: 1100 },
      { name: '市第二人民医院', staffCount: 980 },
      { name: 'A区人民医院', staffCount: 850 },
      { name: 'B区人民医院', staffCount: 720 },
      { name: '市疾控中心', staffCount: 680 },
      { name: 'C区人民医院', staffCount: 580 },
      { name: '市卫生监督所', staffCount: 450 }
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
      yAxis: { type: 'value', name: '人员数' },
      series: [{
        type: 'bar',
        data: data.map(d => d.staffCount),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#91cc75' },
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载人员列表
const loadStaffList = async () => {
  loading.value = true
  try {
    const res = await getStaffList({
      year: queryParams.year,
      region: queryParams.region.join(','),
      staffType: queryParams.staffType,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    staffList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载人员列表失败:', error)
    staffList.value = [
      { staffName: '张明', institutionName: '市人民医院', staffType: '执业医师', title: '正高级', education: '博士', department: '心血管内科', gender: '男', age: 52, yearsOfService: 28, region: 'A区' },
      { staffName: '李芳', institutionName: '市人民医院', staffType: '注册护士', title: '中级', education: '本科', department: '心血管内科', gender: '女', age: 35, yearsOfService: 12, region: 'A区' },
      { staffName: '王强', institutionName: '市中医院', staffType: '执业医师', title: '副高级', education: '硕士', department: '中医科', gender: '男', age: 42, yearsOfService: 18, region: 'B区' }
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
  queryParams.staffType = ''
  queryParams.pageNum = 1
  loadAllData()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadStaffList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadStaffList()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadTypeChart()
  loadTitleChart()
  loadEducationChart()
  loadRatioChart()
  loadTrendChart()
  loadDistributionChart()
  loadInstitutionChart()
  loadStaffList()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 监听人员类型变化
watch(() => queryParams.staffType, () => {
  queryParams.pageNum = 1
  loadStaffList()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.staff-container {
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
  margin-left: 5px;}
</style>