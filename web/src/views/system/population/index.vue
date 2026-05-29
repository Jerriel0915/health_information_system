<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>年龄分布<el-button text size="small" style="float: right;" @click="renderAgeChart">刷新</el-button></template><div ref="ageChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>性别比例<el-button text size="small" style="float: right;" @click="renderGenderChart">刷新</el-button></template><div ref="genderChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="区域ID" prop="regionId"><el-input v-model="queryParams.regionId" placeholder="请输入区域ID" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="区域ID" prop="regionId" /><el-table-column label="总人口" prop="totalPopulation" /><el-table-column label="男性人口" prop="malePopulation" /><el-table-column label="女性人口" prop="femalePopulation" /><el-table-column label="0-14岁" prop="age014" /><el-table-column label="15-59岁" prop="age1559" /><el-table-column label="60岁以上" prop="age60Plus" /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="区域ID" prop="regionId"><el-input v-model="form.regionId" /></el-form-item>
        <el-form-item label="总人口" prop="totalPopulation"><el-input v-model="form.totalPopulation" /></el-form-item>
        <el-form-item label="男性人口" prop="malePopulation"><el-input v-model="form.malePopulation" /></el-form-item>
        <el-form-item label="女性人口" prop="femalePopulation"><el-input v-model="form.femalePopulation" /></el-form-item>
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
import { listPopulation, addPopulation, updatePopulation, delPopulation, getPopulationSummary } from '@/api/system/population'

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
    const res = await getPopulationSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const items = []
      const titleMap = {
        totalPopulation: '总人口', malePopulation: '男性人口', femalePopulation: '女性人口'
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
    const res = await listPopulation(queryParams.value)
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
        const res = form.value.id ? await updatePopulation(form.value) : await addPopulation(form.value)
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
    const res = await delPopulation(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

import { getPopulationAgeDistribution, getPopulationGenderRatio } from '@/api/system/population'

const ageChartRef = ref(null)
const genderChartRef = ref(null)

let ageChart = null
let genderChart = null


const renderChart1 = async () => {
  if (!ageChartRef.value) return
  try {
    const res = await getPopulationAgeDistribution()
    const data = res.code === 200 ? (res.data || {}) : {}
    if (ageChart) ageChart.dispose()
    ageChart = echarts.init(ageChartRef.value)
    const chartData = [
      { name: '0-14岁', value: data.age0_14 || 0 },
      { name: '15-59岁', value: data.age15_59 || 0 },
      { name: '60岁以上', value: data.age60_plus || 0 }
    ].filter(d => d.value > 0)
    ageChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: chartData.length ? chartData : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) { console.error('加载年龄分布失败', e) }
}

const renderChart2 = async () => {
  if (!genderChartRef.value) return
  try {
    const res = await getPopulationGenderRatio()
    const data = res.code === 200 ? (res.data || {}) : {}
    if (genderChart) genderChart.dispose()
    genderChart = echarts.init(genderChartRef.value)
    const chartData = [
      { name: '男性', value: data.maleCount || data.malePopulation || 0 },
      { name: '女性', value: data.femaleCount || data.femalePopulation || 0 }
    ].filter(d => d.value > 0)
    genderChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: chartData.length ? chartData : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) { console.error('加载性别比例失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { ageChart?.resize(); genderChart?.resize() })
})
onBeforeUnmount(() => { ageChart?.dispose(); genderChart?.dispose() })
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



