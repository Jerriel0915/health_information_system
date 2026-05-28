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

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>

    <el-table v-loading="loading" :data="costList" @selection-change="handleSelectionChange">
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const stats = ref([{ title: '费用总额', value: 317828163 }, { title: '医保支付', value: 156084720 }, { title: '自费金额', value: 161743443 }, { title: '医保占比', value: 49.1 }])

const compositionChartRef = ref(null); const trendChartRef = ref(null); let compositionChart = null; let trendChart = null
const renderCompositionChart = () => { if (!compositionChartRef.value) return; if (compositionChart) compositionChart.dispose(); compositionChart = echarts.init(compositionChartRef.value); compositionChart.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' }, series: [{ type: 'pie', radius: '55%', data: [{ name: '药品费', value: 10170 }, { name: '治疗费', value: 8899 }, { name: '检查费', value: 5721 }, { name: '手术费', value: 3814 }, { name: '床位费', value: 2225 }, { name: '护理费', value: 1271 }], label: { show: true, formatter: '{b}: {d}%' } }] }) }
const renderTrendChart = () => { if (!trendChartRef.value) return; if (trendChart) trendChart.dispose(); trendChart = echarts.init(trendChartRef.value); trendChart.setOption({ tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: ['2021', '2022', '2023', '2024', '2025'] }, yAxis: { type: 'value', name: '费用（万元）' }, series: [{ type: 'line', data: [28500, 29800, 30500, 31200, 31783], smooth: true, lineStyle: { color: '#409EFF', width: 3 }, areaStyle: { opacity: 0.3 } }] }) }

const formatMoney = (val) => { if (!val) return 0; if (val >= 10000) return '¥' + (val / 10000).toFixed(1) + '万'; return '¥' + val.toLocaleString() }

const loading = ref(false); const costList = ref([]); const total = ref(0); const showSearch = ref(true); const single = ref(true); const multiple = ref(true); const ids = ref([]); const dialogVisible = ref(false); const dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, serviceId: '' })
const form = ref({}); const formRef = ref(null); const queryFormRef = ref(null)
const rules = { serviceId: [{ required: true, message: '请填写服务ID' }] }

const getList = () => { loading.value = true; setTimeout(() => { costList.value = [{ id: 1, serviceId: 'S001', totalCost: 5000, drugCost: 2000, treatmentCost: 1500, insurancePaid: 3500, selfPaid: 1500 }]; total.value = 1; loading.value = false }, 500) }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加费用'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = '修改费用'; dialogVisible.value = true }
const submitForm = () => { formRef.value?.validate(valid => { if (valid) { ElMessage.success('操作成功'); dialogVisible.value = false; getList() } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(() => { ElMessage.success('删除成功'); getList() }).catch(() => {}) }

onMounted(() => { getList(); renderCompositionChart(); renderTrendChart(); window.addEventListener('resize', () => { compositionChart?.resize(); trendChart?.resize() }) })
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