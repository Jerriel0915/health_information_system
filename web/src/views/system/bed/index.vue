<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards"><el-col :span="6" v-for="stat in stats" :key="stat.key"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ stat.title }}</div><div class="stat-value">{{ stat.value }}<span class="unit">{{ stat.unit }}</span></div></div></el-card></el-col></el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>床位按科室分布<el-button text size="small" style="float: right;" @click="renderDeptChart">刷新</el-button></template>
          <div ref="deptChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>床位使用率趋势<el-button text size="small" style="float: right;" @click="renderTrendChart">刷新</el-button></template>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="机构ID" prop="orgId"><el-input v-model="queryParams.orgId" placeholder="请输入机构ID" clearable /></el-form-item>
      <el-form-item label="科室类型" prop="deptType"><el-input v-model="queryParams.deptType" placeholder="请输入科室类型" clearable /></el-form-item>
      <el-form-item label="统计年份" prop="statYear"><el-input v-model="queryParams.statYear" placeholder="请输入统计年份" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
      
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="机构ID" prop="orgId" />
      <el-table-column label="床位总数" prop="bedCount" />
      <el-table-column label="实际开放床位" prop="actualBedCount" />
      <el-table-column label="使用床位" prop="usedBedCount" />
      <el-table-column label="使用率(%)" prop="bedUsageRate" />
      <el-table-column label="科室类型" prop="deptType" />
      <el-table-column label="统计年份" prop="statYear" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <!-- 对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="机构ID" prop="orgId"><el-input v-model="form.orgId" /></el-form-item>
        <el-form-item label="床位总数" prop="bedCount"><el-input v-model="form.bedCount" /></el-form-item>
        <el-form-item label="实际开放床位" prop="actualBedCount"><el-input v-model="form.actualBedCount" /></el-form-item>
        <el-form-item label="使用床位" prop="usedBedCount"><el-input v-model="form.usedBedCount" /></el-form-item>
        <el-form-item label="床位使用率" prop="bedUsageRate"><el-input v-model="form.bedUsageRate" /></el-form-item>
        <el-form-item label="科室类型" prop="deptType"><el-input v-model="form.deptType" /></el-form-item>
        <el-form-item label="统计年份" prop="statYear"><el-input v-model="form.statYear" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
const showSearch = ref(true)
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listBed, addBed, updateBed, delBed, getBedSummary } from '@/api/system/bed'

// 统计卡片
const stats = ref([])

// 列表
const list = ref([])
const total = ref(0)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10 })
const form = ref({})
const formRef = ref(null)
const rules = {}

// 加载统计卡片
const loadSummary = async () => {
  try {
    const res = await getBedSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const items = []
      const titleMap = {
        totalBedCount: '总床位数', totalActualBedCount: '实际开放', totalUsedBedCount: '使用床位', avgUsageRate: '床位使用率(%)'
      }
      for (const [key, value] of Object.entries(d)) {
        items.push({ key, title: titleMap[key] || key, value: value, unit: '' })
      }
      stats.value = items.slice(0, 4)
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// CRUD
const getList = async () => {
  loading.value = true
  try {
    const res = await listBed(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('查询列表失败', e)
  }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNum: 1, pageSize: 10 }; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row ? { ...row } : {}; dialogTitle.value = '修改'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = form.value.id ? await updateBed(form.value) : await addBed(form.value)
        if (res.code === 200) {
          ElMessage.success('操作成功')
          dialogVisible.value = false
          getList()
        }
      } catch (e) {
        console.error('保存失败', e)
      }
    }
  })
}
const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delBed(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

import { getBedDeptDistribution, getBedTrend } from '@/api/system/bed'

const deptChartRef = ref(null)
const trendChartRef = ref(null)
let deptChart = null
let trendChart = null

const renderChart1 = async () => {
  if (!deptChartRef.value) return
  try {
    const res = await getBedDeptDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (deptChart) deptChart.dispose()
    deptChart = echarts.init(deptChartRef.value)
    deptChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.map(d => d.name) || [] },
      yAxis: { type: 'value', name: '床位数' },
      series: [{ type: 'bar', data: data.map(d => d.value) || [], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
    })
  } catch (e) { console.error('加载科室分布失败', e) }
}

const renderChart2 = async () => {
  if (!trendChartRef.value) return
  try {
    const res = await getBedTrend()
    const data = res.code === 200 ? (res.data || []) : []
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(d => d.year || d.name) || [], name: '年份' },
      yAxis: { type: 'value', name: '床位数' },
      series: [{ type: 'line', data: data.map(d => d.bedCount || d.value) || [], smooth: true, lineStyle: { color: '#67C23A', width: 3 }, areaStyle: { opacity: 0.3 } }]
    })
  } catch (e) { console.error('加载床位趋势失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { deptChart?.resize(); trendChart?.resize() })
})
onBeforeUnmount(() => { deptChart?.dispose(); trendChart?.dispose() })
</script>

<style scoped>
.page-container { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { cursor: pointer; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-card-inner { text-align: center; padding: 8px 0; }
.stat-title { font-size: 14px; color: #909399; margin-bottom: 12px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.unit { font-size: 14px; font-weight: normal; margin-left: 4px; }
.chart-row { margin-bottom: 20px; }
.mb8 { margin-bottom: 8px; }
</style>



