<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>服务类型分布</template><div ref="typeChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>科室服务量分布</template><div ref="deptChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="服务类型"><el-input v-model="queryParams.serviceType" placeholder="请输入服务类型" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="serviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="服务流水号" prop="serviceCode" />
      <el-table-column label="患者姓名" prop="patientName" />
      <el-table-column label="服务类型" prop="serviceType" />
      <el-table-column label="就诊科室" prop="department" />
      <el-table-column label="服务日期" prop="serviceDate" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务流水号" prop="serviceCode"><el-input v-model="form.serviceCode" /></el-form-item>
        <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>
        <el-form-item label="服务类型" prop="serviceType"><el-input v-model="form.serviceType" /></el-form-item>
        <el-form-item label="就诊科室"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="服务日期"><el-input v-model="form.serviceDate" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listService, addService, updateService, delService, getServiceSummary, getServiceTypeDistribution, getServiceDeptDistribution } from '@/api/system/service'

const statData = ref([{ title: '服务总量', value: 0, unit: '人次' }, { title: '门诊量', value: 0, unit: '人次' }, { title: '住院量', value: 0, unit: '人次' }, { title: '急诊量', value: 0, unit: '人次' }])
const typeChartRef = ref(null), deptChartRef = ref(null)
let typeChart = null, deptChart = null
const loading = ref(false), serviceList = ref([]), total = ref(0), single = ref(true), multiple = ref(true), ids = ref([]), dialogVisible = ref(false), dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, serviceType: '' })
const form = ref({}), formRef = ref(null)
const rules = { serviceCode: [{ required: true, message: '请填写服务流水号' }] }

const formatNumber = (val) => { if (!val && val !== 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '\u4e07'; return val.toLocaleString() }

const loadSummary = async () => {
  try { const d = await getServiceSummary(); statData.value[0].value = d.totalCount || d.total || 0; statData.value[1].value = d.outpatientCount || 0; statData.value[2].value = d.inpatientCount || 0; statData.value[3].value = d.emergencyCount || 0 } catch (e) {}
}

const loadTypeChart = async () => {
  try {
    const data = await getServiceTypeDistribution()
    if (!typeChartRef.value) return; if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    typeChart.setOption({
      tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.serviceType || d.key, value: d.value || d.count })) : [{ name: '暂无', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) {}
}

const loadDeptChart = async () => {
  try {
    const data = await getServiceDeptDistribution()
    if (!deptChartRef.value) return; if (deptChart) deptChart.dispose()
    deptChart = echarts.init(deptChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const names = list.map(d => d.name || d.department || d.key)
    const vals = list.map(d => d.value || d.count)
    deptChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: names.length ? names : ['暂无'], axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: '服务量' },
      series: [{ type: 'bar', data: vals.length ? vals : [0], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
    })
  } catch (e) {}
}

const getList = async () => {
  loading.value = true
  try { const res = await listService(queryParams); serviceList.value = res.rows || res.records || res.list || []; total.value = res.total || serviceList.value.length } catch (e) { serviceList.value = []; total.value = 0 }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryParams.serviceType = ''; handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加服务记录'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改服务记录'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try { if (form.value.id) await updateService(form.value); else await addService(form.value); ElMessage.success('操作成功'); dialogVisible.value = false; getList() } catch (e) { ElMessage.error('操作失败') }
  })
}
const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(async () => { try { await delService(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') } }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); loadTypeChart(); loadDeptChart(); window.addEventListener('resize', () => { typeChart?.resize(); deptChart?.resize() }) })
onBeforeUnmount(() => { typeChart?.dispose(); deptChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
