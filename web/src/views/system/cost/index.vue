<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatMoney(item.value) }}</div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>费用构成<el-button text size="small" style="float: right;" @click="renderCompositionChart">刷新</el-button></template><div ref="compositionChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>费用趋势<el-button text size="small" style="float: right;" @click="renderTrendChart">刷新</el-button></template><div ref="trendChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="服务ID" prop="serviceId"><el-input v-model="queryParams.serviceId" placeholder="请输入服务ID" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="服务ID" prop="serviceId" /><el-table-column label="费用总额" prop="totalCost" /><el-table-column label="药品费" prop="drugCost" /><el-table-column label="治疗费" prop="treatmentCost" /><el-table-column label="医保支付" prop="insurancePaid" /><el-table-column label="自费金额" prop="selfPaid" /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务ID" prop="serviceId"><el-input v-model="form.serviceId" /></el-form-item>
        <el-form-item label="费用总额" prop="totalCost"><el-input v-model="form.totalCost" /></el-form-item>
        <el-form-item label="医保支付" prop="insurancePaid"><el-input v-model="form.insurancePaid" /></el-form-item>
        <el-form-item label="自费金额" prop="selfPaid"><el-input v-model="form.selfPaid" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listCost, addCost, updateCost, delCost, getCostSummary, getCostComposition, getCostTrend } from '@/api/system/cost'

const showSearch = ref(true)

// 统计卡片
const stats = ref([])

const formatMoney = (val) => {
  if (!val && val !== 0) return '--'
  if (val >= 100000000) return '¥' + (val / 100000000).toFixed(2) + '亿'
  if (val >= 10000) return '¥' + (val / 10000).toFixed(1) + '万'
  return '¥' + Number(val).toLocaleString()
}

// 列表
const list = ref([])
const total = ref(0)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, serviceId: '' })
const form = ref({})
const formRef = ref(null)
const rules = { serviceId: [{ required: true, message: '请填写服务ID' }] }

// 图表
const compositionChartRef = ref(null)
const trendChartRef = ref(null)
let compositionChart = null
let trendChart = null

// 加载统计
const loadSummary = async () => {
  try {
    const res = await getCostSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      stats.value = [
        { title: '费用总额', value: d.totalCost || 0 },
        { title: '医保支付', value: d.totalInsurancePaid || 0 },
        { title: '自费金额', value: d.totalSelfPaid || 0 },
        { title: '总记录数', value: d.totalRecords || 0 }
      ]
    }
  } catch (e) { console.error('加载费用统计失败', e) }
}

// 图表渲染
const renderCompositionChart = async () => {
  if (!compositionChartRef.value) return
  try {
    const res = await getCostComposition()
    const data = res.code === 200 ? (res.data || {}) : {}
    if (compositionChart) compositionChart.dispose()
    compositionChart = echarts.init(compositionChartRef.value)
    const chartData = [
      { name: '药品费', value: data.drugCost || 0 },
      { name: '治疗费', value: data.treatmentCost || 0 },
      { name: '检查费', value: data.inspectionCost || 0 },
      { name: '手术费', value: data.surgeryCost || 0 },
      { name: '床位费', value: data.bedCost || 0 },
      { name: '护理费', value: data.nursingCost || 0 }
    ].filter(d => d.value > 0)
    compositionChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: chartData.length ? chartData : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) { console.error('加载费用构成失败', e) }
}

const renderTrendChart = async () => {
  if (!trendChartRef.value) return
  try {
    const res = await getCostTrend()
    const data = res.code === 200 ? (res.data || []) : []
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['总费用', '医保支付', '自费金额'] },
      xAxis: { type: 'category', data: data.map(d => d.year || d.name) || [], name: '年份' },
      yAxis: { type: 'value', name: '金额（元）' },
      series: [
        { name: '总费用', type: 'line', data: data.map(d => d.totalCost) || [], smooth: true, lineStyle: { color: '#409EFF', width: 3 }, areaStyle: { opacity: 0.2 } },
        { name: '医保支付', type: 'line', data: data.map(d => d.insurancePaid) || [], smooth: true, lineStyle: { color: '#67C23A', width: 2 } },
        { name: '自费金额', type: 'line', data: data.map(d => d.selfPaid) || [], smooth: true, lineStyle: { color: '#E6A23C', width: 2 } }
      ]
    })
  } catch (e) { console.error('加载费用趋势失败', e) }
}

// CRUD
const getList = async () => {
  loading.value = true
  try {
    const res = await listCost(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) { console.error('查询费用列表失败', e) }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNum: 1, pageSize: 10, serviceId: '' }; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加费用'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row ? { ...row } : {}; dialogTitle.value = '修改费用'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = form.value.id ? await updateCost(form.value) : await addCost(form.value)
        if (res.code === 200) {
          ElMessage.success('操作成功')
          dialogVisible.value = false
          getList()
        }
      } catch (e) { console.error('保存费用失败', e) }
    }
  })
}
const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delCost(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) { if (e !== 'cancel') console.error('删除失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderCompositionChart(), renderTrendChart()])
  getList()
})
onBeforeUnmount(() => { compositionChart?.dispose(); trendChart?.dispose() })
</script>

<style scoped>
.page-container { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { cursor: pointer; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-card-inner { text-align: center; padding: 8px 0; }
.stat-title { font-size: 14px; color: #909399; margin-bottom: 12px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.chart-row { margin-bottom: 20px; }
.mb8 { margin-bottom: 8px; }
</style>


