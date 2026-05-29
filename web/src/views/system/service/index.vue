<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>服务类型分布<el-button text size="small" style="float: right;" @click="renderTypeChart">刷新</el-button></template><div ref="typeChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>服务趋势<el-button text size="small" style="float: right;" @click="renderTrendChart">刷新</el-button></template><div ref="trendChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="患者姓名" prop="patientName"><el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="患者姓名" prop="patientName" /><el-table-column label="性别" prop="patientGender" /><el-table-column label="年龄" prop="patientAge" /><el-table-column label="服务类型" prop="serviceType" /><el-table-column label="科室" prop="department" /><el-table-column label="诊断名称" prop="diagnosisName" show-overflow-tooltip /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>
        <el-form-item label="性别" prop="patientGender"><el-select v-model="form.patientGender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
        <el-form-item label="年龄" prop="patientAge"><el-input v-model="form.patientAge" /></el-form-item>
        <el-form-item label="服务类型" prop="serviceType"><el-select v-model="form.serviceType"><el-option label="门诊" value="门诊" /><el-option label="住院" value="住院" /><el-option label="急诊" value="急诊" /></el-select></el-form-item>
        <el-form-item label="科室" prop="department"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="诊断名称" prop="diagnosisName"><el-input v-model="form.diagnosisName" /></el-form-item>
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
import { listService, addService, updateService, delService, getServiceSummary } from '@/api/system/service'

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
    const res = await getServiceSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const items = []
      const titleMap = {
        totalServices: '服务总数', typeCount: '服务类型', outpatientCount: '门诊次数', inpatientCount: '住院次数', avgDaysInHospital: '平均住院天数'
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
    const res = await listService(queryParams.value)
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
        const res = form.value.id ? await updateService(form.value) : await addService(form.value)
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
    const res = await delService(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

import { getServiceTypeDistribution, getServiceTrend } from '@/api/system/service'

const typeChartRef = ref(null)
const trendChartRef = ref(null)

let typeChart = null
let trendChart = null


const renderChart1 = async () => {
  if (!typeChartRef.value) return
  try {
    const res = await getServiceTypeDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    const sorted = [...data].sort((a,b) => b.value - a.value)
    typeChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 80, right: 40, top: 20, bottom: 30 },
      xAxis: { type: 'value', name: '服务量' },
      yAxis: { type: 'category', data: sorted.map(d => d.name).reverse(), axisLabel: { fontSize: 11 } },
      series: [{
        type: 'bar',
        data: sorted.map(d => d.value).reverse(),
        barWidth: 14,
        itemStyle: { borderRadius: [0, 4, 4, 0], color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#79BBFF' }]) }
      }]
    })
  } catch (e) { console.error('加载服务类型分布失败', e) }
}

const renderChart2 = async () => {
  if (!trendChartRef.value) return
  try {
    const res = await getServiceTrend()
    const data = res.code === 200 ? (res.data || []) : []
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(d => d.year || d.name) || [], name: '年份' },
      yAxis: { type: 'value', name: '服务量' },
      series: [{ type: 'line', data: data.map(d => d.value) || [], smooth: true, lineStyle: { color: '#67C23A', width: 3 }, areaStyle: { opacity: 0.3 } }]
    })
  } catch (e) { console.error('加载服务趋势失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { typeChart?.resize(); trendChart?.resize() })
})
onBeforeUnmount(() => { typeChart?.dispose(); trendChart?.dispose() })
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



