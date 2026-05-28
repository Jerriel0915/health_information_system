<template>
  <div class="institution-container">
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
            <el-option label="专业公共卫生机构" value="public_health" />
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
            <div class="indicator-title">医疗机构总数</div>
            <div class="indicator-value">{{ overviewData.totalInstitutions?.toLocaleString() || '--' }}</div>
            <div class="indicator-trend">
              <span>同比去年</span>
              <span :class="overviewData.institutionGrowth >= 0 ? 'trend-up' : 'trend-down'">
                {{ formatGrowth(overviewData.institutionGrowth) }}
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">医院</div>
            <div class="indicator-value">{{ overviewData.hospitalCount?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">三级医院 {{ overviewData.tertiaryHospitalCount || 0 }} 家</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">基层医疗卫生机构</div>
            <div class="indicator-value">{{ overviewData.primaryCount?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">社区卫生服务中心 {{ overviewData.communityCount || 0 }} 家</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-title">专业公共卫生机构</div>
            <div class="indicator-value">{{ overviewData.publicHealthCount?.toLocaleString() || '--' }}</div>
            <div class="indicator-sub">疾控/妇幼/卫监</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 机构类型分布柱状图 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">机构类型分布</span>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 机构等级分布饼图 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医院等级分布</span>
          </div>
          <div ref="levelChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 机构数量趋势 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">医疗机构数量变化趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container-large"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 机构床位关联分析 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">机构服务能力排行</span>
          </div>
          <div ref="capacityChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 机构区域分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">各区县机构数量</span>
          </div>
          <div ref="distributionChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 机构详情表格 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">医疗机构详细信息</span>
      </div>
      <el-table :data="institutionList" v-loading="loading" stripe>
        <el-table-column prop="institutionName" label="机构名称" min-width="200" />
        <el-table-column prop="institutionType" label="机构类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.institutionType)">
              {{ scope.row.institutionType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.level" :type="getLevelTagType(scope.row.level)">
              {{ scope.row.level }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="bedCount" label="床位数" sortable width="100">
          <template #default="scope">
            {{ scope.row.bedCount?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="staffCount" label="卫生人员数" sortable width="120">
          <template #default="scope">
            {{ scope.row.staffCount?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="region" label="所在区域" width="120" />
        <el-table-column prop="address" label="地址" min-width="200" />
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
  getInstitutionOverview,
  getInstitutionType,
  getInstitutionLevel,
  getInstitutionTrend,
  getInstitutionCapacity,
  getInstitutionDistribution
} from '@/api/statistics/institution'

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
      { value: 'district4', label: 'D区' }
    ]}
])

// 数据存储
const overviewData = ref({})
const institutionList = ref([])
const total = ref(0)

// 图表实例
const typeChartRef = ref(null)
const levelChartRef = ref(null)
const trendChartRef = ref(null)
const capacityChartRef = ref(null)
const distributionChartRef = ref(null)
let typeChart = null
let levelChart = null
let trendChart = null
let capacityChart = null
let distributionChart = null

// 格式化函数
const formatGrowth = (value) => {
  if (value === undefined || value === null) return '--'
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${value}%`
}

const getTypeTagType = (type) => {
  const map = {
    '医院': 'primary',
    '基层医疗卫生机构': 'success',
    '专业公共卫生机构': 'warning'
  }
  return map[type] || 'info'
}

const getLevelTagType = (level) => {
  if (level === '三级医院') return 'danger'
  if (level === '二级医院') return 'warning'
  if (level === '一级医院') return 'success'
  return 'info'
}

// 加载概况数据
const loadOverview = async () => {
  try {
    const res = await getInstitutionOverview({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    overviewData.value = res.data
  } catch (error) {
    console.error('加载机构概况失败:', error)
    overviewData.value = {
      totalInstitutions: 3850,
      hospitalCount: 185,
      tertiaryHospitalCount: 28,
      secondaryHospitalCount: 62,
      primaryHospitalCount: 95,
      primaryCount: 3420,
      communityCount: 156,
      publicHealthCount: 245,
      institutionGrowth: 2.5
    }
  }
}

// 加载机构类型分布图
const loadTypeChart = async () => {
  try {
    const res = await getInstitutionType({ year: queryParams.year })
    const data = res.data || [
      { name: '医院', value: 185 },
      { name: '基层医疗卫生机构', value: 3420 },
      { name: '专业公共卫生机构', value: 245 }
    ]
    renderTypeChart(data)
  } catch (error) {
    renderTypeChart([
      { name: '医院', value: 185 },
      { name: '基层医疗卫生机构', value: 3420 },
      { name: '专业公共卫生机构', value: 245 }
    ])
  }
}

const renderTypeChart = (data) => {
  nextTick(() => {
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name), name: '机构类型' },
      yAxis: { type: 'value', name: '数量（家）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#5470c6' },
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载机构等级分布图
const loadLevelChart = async () => {
  try {
    const res = await getInstitutionLevel({ year: queryParams.year })
    const data = res.data || [
      { name: '三级医院', value: 28 },
      { name: '二级医院', value: 62 },
      { name: '一级医院', value: 95 }
    ]
    renderLevelChart(data)
  } catch (error) {
    renderLevelChart([
      { name: '三级医院', value: 28 },
      { name: '二级医院', value: 62 },
      { name: '一级医院', value: 95 }
    ])
  }
}

const renderLevelChart = (data) => {
  nextTick(() => {
    if (levelChart) levelChart.dispose()
    levelChart = echarts.init(levelChartRef.value)
    levelChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: data.map(item => ({ name: item.name, value: item.value })),
        label: { formatter: '{b}: {d}%' },
        color: ['#f56c6c', '#e6a23c', '#67c23a']
      }]
    })
  })
}

// 加载机构趋势图
const loadTrendChart = async () => {
  try {
    const res = await getInstitutionTrend({
      year: queryParams.year,
      region: queryParams.region.join(',')
    })
    const data = res.data || {
      years: ['2017', '2018', '2019', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      hospitalCount: [165, 168, 172, 175, 178, 180, 182, 184, 185, 186],
      primaryCount: [2980, 3050, 3120, 3180, 3250, 3300, 3350, 3390, 3410, 3420]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      years: ['2017', '2018', '9', '2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      hospitalCount: [165, 168, 172, 175, 178, 180, 182, 184, 185, 186],
      primaryCount: [2980, 3050, 3120, 3180, 3250, 3300, 3350, 3390, 3410, 3420]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['医院', '基层医疗卫生机构'] },
      xAxis: { type: 'category', data: data.years, name: '年份' },
      yAxis: { type: 'value', name: '数量（家）' },
      series: [
        { name: '医院', type: 'line', data: data.hospitalCount, smooth: true, lineStyle: { width: 3 }, symbol: 'circle' },
        { name: '基层医疗卫生机构', type: 'line', data: data.primaryCount, smooth: true, lineStyle: { width: 3 }, symbol: 'diamond' }
      ]
    })
  })
}

// 加载服务能力排行图
const loadCapacityChart = async () => {
  try {
    const res = await getInstitutionCapacity({ year: queryParams.year })
    const data = res.data || [
      { name: '市人民医院', beds: 3200, staff: 4500 },
      { name: '市中医院', beds: 1800, staff: 2200 },
      { name: '市中心医院', beds: 1500, staff: 1900 },
      { name: '市妇幼保健院', beds: 800, staff: 1100 },
      { name: '市第二人民医院', beds: 750, staff: 980 }
    ]
    renderCapacityChart(data)
  } catch (error) {
    renderCapacityChart([
      { name: '市人民医院', beds: 3200, staff: 4500 },
      { name: '市中医院', beds: 1800, staff: 2200 },
      { name: '市中心医院', beds: 1500, staff: 1900 },
      { name: '市妇幼保健院', beds: 800, staff: 1100 },
      { name: '市第二人民医院', beds: 750, staff: 980 }
    ])
  }
}

const renderCapacityChart = (data) => {
  nextTick(() => {
    if (capacityChart) capacityChart.dispose()
    capacityChart = echarts.init(capacityChartRef.value)
    capacityChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['床位数', '卫生人员数'] },
      xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value' },
      series: [
        { name: '床位数', type: 'bar', data: data.map(d => d.beds), itemStyle: { color: '#5470c6' }, label: { show: true, position: 'top' } },
        { name: '卫生人员数', type: 'bar', data: data.map(d => d.staff), itemStyle: { color: '#fac858' }, label: { show: true, position: 'top' } }
      ]
    })
  })
}

// 加载区域分布图
const loadDistributionChart = async () => {
  try {
    const res = await getInstitutionDistribution({ year: queryParams.year })
    const data = res.data || [
      { name: 'A区', value: 850 },
      { name: 'B区', value: 720 },
      { name: 'C区', value: 580 },
      { name: 'D区', value: 490 },
      { name: 'E区', value: 420 },
      { name: 'F区', value: 380 }
    ]
    renderDistributionChart(data)
  } catch (error) {
    renderDistributionChart([
      { name: 'A区', value: 850 },
      { name: 'B区', value: 720 },
      { name: 'C区', value: 580 },
      { name: 'D区', value: 490 },
      { name: 'E区', value: 420 },
      { name: 'F区', value: 380 }
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
      yAxis: { type: 'value', name: '机构数量（家）' },
      series: [{
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#91cc75' },
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载机构列表
const loadInstitutionList = async () => {
  loading.value = true
  try {
    const res = await getInstitutionDistribution({
      year: queryParams.year,
      region: queryParams.region.join(','),
      institutionType: queryParams.institutionType,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    institutionList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载机构列表失败:', error)
    institutionList.value = [
      { institutionName: '市人民医院', institutionType: '医院', level: '三级医院', bedCount: 3200, staffCount: 4500, region: 'A区', address: 'A区人民路1号' },
      { institutionName: '市中医院', institutionType: '医院', level: '三级医院', bedCount: 1800, staffCount: 2200, region: 'B区', address: 'B区解放路88号' },
      { institutionName: 'A区社区卫生服务中心', institutionType: '基层医疗卫生机构', level: '', bedCount: 50, staffCount: 120, region: 'A区', address: 'A区建设路10号' }
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
  loadInstitutionList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadInstitutionList()
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadTypeChart()
  loadLevelChart()
  loadTrendChart()
  loadCapacityChart()
  loadDistributionChart()
  loadInstitutionList()
}

// 监听年份变化
watch(() => queryParams.year, () => {
  loadAllData()
})

// 监听机构类型变化
watch(() => queryParams.institutionType, () => {
  queryParams.pageNum = 1
  loadInstitutionList()
})

// 初始化
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.institution-container {
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