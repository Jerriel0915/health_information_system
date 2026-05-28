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

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>

    <el-table v-loading="loading" :data="populationList" @selection-change="handleSelectionChange">
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const stats = ref([{ title: '总人口', value: 8500000, unit: '人' }, { title: '男性人口', value: 4318000, unit: '人' }, { title: '女性人口', value: 4182000, unit: '人' }, { title: '性别比', value: 103.3, unit: '' }])

const ageChartRef = ref(null); const genderChartRef = ref(null); let ageChart = null; let genderChart = null
const renderAgeChart = () => { if (!ageChartRef.value) return; if (ageChart) ageChart.dispose(); ageChart = echarts.init(ageChartRef.value); ageChart.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' }, series: [{ type: 'pie', radius: '55%', data: [{ name: '0-14岁', value: 1530000 }, { name: '15-59岁', value: 5525000 }, { name: '60岁以上', value: 1445000 }], label: { show: true, formatter: '{b}: {d}%' } }] }) }
const renderGenderChart = () => { if (!genderChartRef.value) return; if (genderChart) genderChart.dispose(); genderChart = echarts.init(genderChartRef.value); genderChart.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' }, series: [{ type: 'pie', radius: '55%', data: [{ name: '男性', value: 4318000 }, { name: '女性', value: 4182000 }], label: { show: true, formatter: '{b}: {d}%' } }] }) }

const formatNumber = (val) => { if (!val) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '万'; return val.toLocaleString() }

const loading = ref(false); const populationList = ref([]); const total = ref(0); const showSearch = ref(true); const single = ref(true); const multiple = ref(true); const ids = ref([]); const dialogVisible = ref(false); const dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, regionId: '' })
const form = ref({}); const formRef = ref(null); const queryFormRef = ref(null)
const rules = { regionId: [{ required: true, message: '请填写区域ID' }] }

const getList = () => { loading.value = true; setTimeout(() => { populationList.value = [{ id: 1, regionId: '001', totalPopulation: 8500000, malePopulation: 4318000, femalePopulation: 4182000, age014: 1530000, age1559: 5525000, age60Plus: 1445000 }]; total.value = 1; loading.value = false }, 500) }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加人口数据'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = '修改人口数据'; dialogVisible.value = true }
const submitForm = () => { formRef.value?.validate(valid => { if (valid) { ElMessage.success('操作成功'); dialogVisible.value = false; getList() } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(() => { ElMessage.success('删除成功'); getList() }).catch(() => {}) }

onMounted(() => { getList(); renderAgeChart(); renderGenderChart(); window.addEventListener('resize', () => { ageChart?.resize(); genderChart?.resize() }) })
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