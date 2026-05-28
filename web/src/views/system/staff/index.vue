<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>职称分布</template><div ref="titleChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>学历分布</template><div ref="eduChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="姓名"><el-input v-model="queryParams.staffName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="staffList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="姓名" prop="staffName" />
      <el-table-column label="性别" prop="gender" />
      <el-table-column label="科室" prop="department" />
      <el-table-column label="职称" prop="jobTitle" />
      <el-table-column label="学历" prop="education" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="staffName"><el-input v-model="form.staffName" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" :value="1" /><el-option label="女" :value="2" /></el-select></el-form-item>
        <el-form-item label="科室"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.jobTitle" /></el-form-item>
        <el-form-item label="学历"><el-select v-model="form.education"><el-option label="博士" value="博士" /><el-option label="硕士" value="硕士" /><el-option label="本科" value="本科" /><el-option label="大专" value="大专" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listStaff, addStaff, updateStaff, delStaff, getStaffSummary, getStaffJobTitleDistribution, getStaffEducationDistribution } from '@/api/system/staff'

const statData = ref([{ title: '人员总数', value: 0, unit: '人' }, { title: '医生人数', value: 0, unit: '人' }, { title: '护士人数', value: 0, unit: '人' }, { title: '医技人员', value: 0, unit: '人' }])
const titleChartRef = ref(null), eduChartRef = ref(null)
let titleChart = null, eduChart = null
const loading = ref(false), staffList = ref([]), total = ref(0), single = ref(true), multiple = ref(true), ids = ref([]), dialogVisible = ref(false), dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, staffName: '' })
const form = ref({}), formRef = ref(null)
const rules = { staffName: [{ required: true, message: '请填写姓名' }] }

const formatNumber = (val) => { if (!val && val !== 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '\u4e07'; return val.toLocaleString() }

const loadSummary = async () => {
  try {
    const data = await getStaffSummary()
    statData.value[0].value = data.totalCount || data.total || 0
    statData.value[1].value = data.doctorCount || data.doctors || 0
    statData.value[2].value = data.nurseCount || data.nurses || 0
    statData.value[3].value = data.techCount || data.technicians || 0
  } catch (e) {}
}

const loadTitleChart = async () => {
  try {
    const data = await getStaffJobTitleDistribution()
    if (!titleChartRef.value) return
    if (titleChart) titleChart.dispose()
    titleChart = echarts.init(titleChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    titleChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: list.map(d => d.name || d.jobTitle || d.key), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: '人数' },
      series: [{ type: 'bar', data: list.map(d => d.value || d.count), itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' } }]
    })
  } catch (e) {}
}

const loadEduChart = async () => {
  try {
    const data = await getStaffEducationDistribution()
    if (!eduChartRef.value) return
    if (eduChart) eduChart.dispose()
    eduChart = echarts.init(eduChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    eduChart.setOption({
      tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.education || d.key, value: d.value || d.count })) : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) {}
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listStaff(queryParams)
    staffList.value = res.rows || res.records || res.list || []
    total.value = res.total || staffList.value.length
  } catch (e) { staffList.value = []; total.value = 0 }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryParams.staffName = ''; handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加人员'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改人员'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try { if (form.value.id) await updateStaff(form.value); else await addStaff(form.value); ElMessage.success('操作成功'); dialogVisible.value = false; getList() } catch (e) { ElMessage.error('操作失败') }
  })
}
const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(async () => { try { await delStaff(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') } }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); loadTitleChart(); loadEduChart(); window.addEventListener('resize', () => { titleChart?.resize(); eduChart?.resize() }) })
onBeforeUnmount(() => { titleChart?.dispose(); eduChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
