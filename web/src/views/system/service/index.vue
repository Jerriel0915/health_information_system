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

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>

    <el-table v-loading="loading" :data="serviceList" @selection-change="handleSelectionChange">
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const stats = ref([{ title: '服务总量', value: 50000, unit: '人次' }, { title: '门诊服务量', value: 32500, unit: '人次' }, { title: '住院服务量', value: 17500, unit: '人次' }, { title: '平均住院天数', value: 8.5, unit: '天' }])

const typeChartRef = ref(null); const trendChartRef = ref(null); let typeChart = null; let trendChart = null
const renderTypeChart = () => { if (!typeChartRef.value) return; if (typeChart) typeChart.dispose(); typeChart = echarts.init(typeChartRef.value); typeChart.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' }, series: [{ type: 'pie', radius: '55%', data: [{ name: '门诊', value: 32500 }, { name: '住院', value: 17500 }], label: { show: true, formatter: '{b}: {d}%' } }] }) }
const renderTrendChart = () => { if (!trendChartRef.value) return; if (trendChart) trendChart.dispose(); trendChart = echarts.init(trendChartRef.value); trendChart.setOption({ tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: ['2021', '2022', '2023', '2024', '2025'] }, yAxis: { type: 'value', name: '服务量' }, series: [{ type: 'line', data: [42000, 44500, 46800, 49200, 50000], smooth: true, lineStyle: { color: '#67C23A', width: 3 }, areaStyle: { opacity: 0.3 } }] }) }

const formatNumber = (val) => { if (!val) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '万'; return val.toLocaleString() }

const loading = ref(false); const serviceList = ref([]); const total = ref(0); const showSearch = ref(true); const single = ref(true); const multiple = ref(true); const ids = ref([]); const dialogVisible = ref(false); const dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, patientName: '' })
const form = ref({}); const formRef = ref(null); const queryFormRef = ref(null)
const rules = { patientName: [{ required: true, message: '请填写患者姓名' }] }

const getList = () => { loading.value = true; setTimeout(() => { serviceList.value = [{ id: 1, patientName: '张三', patientGender: '男', patientAge: 45, serviceType: '门诊', department: '内科', diagnosisName: '高血压' }]; total.value = 1; loading.value = false }, 500) }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加服务记录'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = '修改服务记录'; dialogVisible.value = true }
const submitForm = () => { formRef.value?.validate(valid => { if (valid) { ElMessage.success('操作成功'); dialogVisible.value = false; getList() } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(() => { ElMessage.success('删除成功'); getList() }).catch(() => {}) }

onMounted(() => { getList(); renderTypeChart(); renderTrendChart(); window.addEventListener('resize', () => { typeChart?.resize(); trendChart?.resize() }) })
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