<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>服务类型分布</template><div ref="typeChartRef" style="height: 300px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>服务趋势</template><div ref="trendChartRef" style="height: 300px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="服务类别"><el-input v-model="queryParams.serviceCategory" placeholder="请输入服务类别" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="服务编码" prop="serviceCode" /><el-table-column label="服务类别" prop="serviceCategory" /><el-table-column label="性别"><template #default="{ row }">{{ row.patientGender === 1 ? '男' : row.patientGender === 2 ? '女' : '' }}</template></el-table-column><el-table-column label="年龄" prop="patientAge" /><el-table-column label="服务类型" prop="serviceType" /><el-table-column label="所属机构" prop="orgName" show-overflow-tooltip /><el-table-column label="诊断名称" prop="diagnosisName" show-overflow-tooltip /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="服务编码" prop="serviceCode"><el-input v-model="form.serviceCode" placeholder="请输入服务编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="服务类别" prop="serviceCategory"><el-input v-model="form.serviceCategory" placeholder="请输入服务类别" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="性别" prop="patientGender"><el-select v-model="form.patientGender" placeholder="请选择性别" style="width: 100%;"><el-option :label="'男'" :value="1" /><el-option :label="'女'" :value="2" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年龄"><el-input v-model="form.patientAge" placeholder="请输入年龄" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="服务类型" prop="serviceType"><el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width: 100%;"><el-option label="门诊" value="门诊" /><el-option label="住院" value="住院" /><el-option label="急诊" value="急诊" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属机构" prop="orgId"><el-select v-model="form.orgId" placeholder="请选择机构" style="width: 100%;" filterable><el-option v-for="item in orgOptions" :key="item.id" :label="item.orgName" :value="item.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="服务日期" prop="serviceDate"><el-date-picker v-model="form.serviceDate" type="date" placeholder="选择服务日期" style="width: 100%;" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="科室"><el-input v-model="form.department" placeholder="请输入科室" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="诊断名称"><el-input v-model="form.diagnosisName" placeholder="请输入诊断名称" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listService, addService, updateService, delService, getServiceSummary, getServiceTypeDistribution, getServiceTrend } from '@/api/system/service'
import { listInstitution } from '@/api/system/institution'

const stats = ref([])
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
const submitLoading = ref(false)
const orgOptions = ref([])
const rules = { serviceCode: [{ required: true, message: '请填写服务编码' }], serviceCategory: [{ required: true, message: '请填写服务类别' }], serviceType: [{ required: true, message: '请选择服务类型' }] }

const formatNumber = (val) => {
  if (!val && val !== 0) return '--'
  return Number(val).toLocaleString()
}

const loadSummary = async () => {
  try {
    const res = await getServiceSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const titleMap = { totalServices: '服务总数', typeCount: '服务类型', outpatientCount: '门诊次数', inpatientCount: '住院次数', avgDaysInHospital: '平均住院天数' }
      const items = []
      for (const [key, value] of Object.entries(d)) {
        items.push({ key, title: titleMap[key] || key, value: value, unit: '' })
      }
      stats.value = items.slice(0, 4)
    }
  } catch (e) { console.error('加载统计失败', e) }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listService(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) { console.error('查询列表失败', e) }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNum: 1, pageSize: 10 }; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }

const loadOrgOptions = async () => {
  try {
    const res = await listInstitution({ pageNum: 1, pageSize: 200 })
    if (res.code === 200) { orgOptions.value = res.rows || [] }
  } catch (e) { console.error('加载机构列表失败', e) }
}

const handleAdd = async () => {
  form.value = { serviceStatus: 1 }
  dialogTitle.value = '添加服务'
  dialogVisible.value = true
  await loadOrgOptions()
}
const handleUpdate = async (row) => {
  form.value = {
    id: row.id,
    serviceCode: row.serviceCode,
    serviceCategory: row.serviceCategory,
    patientGender: row.patientGender != null ? Number(row.patientGender) : null,
    patientAge: row.patientAge,
    serviceType: row.serviceType,
    orgId: row.orgId != null ? Number(row.orgId) : null,
    serviceDate: row.serviceDate,
    department: row.department || '',
    diagnosisName: row.diagnosisName || ''
  }
  dialogTitle.value = '修改服务'
  dialogVisible.value = true
  await loadOrgOptions()
}
const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form.value }
      const res = payload.id ? await updateService(payload) : await addService(payload)
      if (res.code === 200) {
        ElMessage.success('操作成功')
          await renderChart1(), renderChart2()
        dialogVisible.value = false
        getList()
      }
    } catch (e) { console.error('保存失败', e) }
    finally { submitLoading.value = false }
  })
}
const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delService(delIds)
    if (res.code === 200) { ElMessage.success('删除成功'); getList()
    await renderChart1(), renderChart2() }
  } catch (e) { if (e !== 'cancel') console.error('删除失败', e) }
}

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
      tooltip: { trigger: 'axis' },
      grid: { left: 80, right: 40, top: 20, bottom: 30 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(d => d.name).reverse(), axisLabel: { fontSize: 11 } },
      series: [{ type: 'bar', data: sorted.map(d => d.value).reverse(), barWidth: 14, itemStyle: { borderRadius: [0, 4, 4, 0] } }]
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
      xAxis: { type: 'category', data: data.map(d => d.year || d.name) || [] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: data.map(d => d.value) || [], smooth: true, areaStyle: { opacity: 0.3 } }]
    })
  } catch (e) { console.error('加载服务趋势失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { typeChart?.resize(); trendChart?.resize() })
})
onBeforeUnmount(() => { typeChart?.dispose(); trendChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
