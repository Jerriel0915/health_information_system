<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>费用构成分析</template><div ref="compositionChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>医保费用分析</template><div ref="insuranceChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="费用类别"><el-input v-model="queryParams.costCategory" placeholder="请输入费用类别" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="costList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="服务ID" prop="serviceId" />
      <el-table-column label="总费用" prop="totalCost" />
      <el-table-column label="药品费" prop="drugCost" />
      <el-table-column label="治疗费" prop="treatmentCost" />
      <el-table-column label="检查费" prop="inspectionCost" />
      <el-table-column label="费用类别" prop="costCategory" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务ID" prop="serviceId"><el-input v-model="form.serviceId" /></el-form-item>
        <el-form-item label="总费用" prop="totalCost"><el-input v-model="form.totalCost" /></el-form-item>
        <el-form-item label="药品费"><el-input v-model="form.drugCost" /></el-form-item>
        <el-form-item label="治疗费"><el-input v-model="form.treatmentCost" /></el-form-item>
        <el-form-item label="检查费"><el-input v-model="form.inspectionCost" /></el-form-item>
        <el-form-item label="费用类别"><el-input v-model="form.costCategory" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listCost, addCost, updateCost, delCost, getCostSummary, getCostComposition, getInsuranceAnalysis } from '@/api/system/cost'

const statData = ref([{ title: '总费用', value: 0, unit: '元' }, { title: '次均费用', value: 0, unit: '元' }, { title: '医保支出', value: 0, unit: '元' }, { title: '自付金额', value: 0, unit: '元' }])
const compositionChartRef = ref(null), insuranceChartRef = ref(null)
let compositionChart = null, insuranceChart = null
const loading = ref(false), costList = ref([]), total = ref(0), single = ref(true), multiple = ref(true), ids = ref([]), dialogVisible = ref(false), dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, costCategory: '' })
const form = ref({}), formRef = ref(null)
const rules = { serviceId: [{ required: true, message: '请填写服务ID' }] }

const formatNumber = (val) => { if (!val && val !== 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '\u4e07'; return val.toLocaleString() }

const loadSummary = async () => {
  try { const d = await getCostSummary(); statData.value[0].value = d.totalCost || d.total || 0; statData.value[1].value = d.avgCost || 0; statData.value[2].value = d.insurancePaid || d.insurance || 0; statData.value[3].value = d.selfPaid || 0 } catch (e) {}
}

const loadCompositionChart = async () => {
  try {
    const data = await getCostComposition()
    if (!compositionChartRef.value) return; if (compositionChart) compositionChart.dispose()
    compositionChart = echarts.init(compositionChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    compositionChart.setOption({
      tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: list.length ? list.map(d => ({ name: d.name || d.costCategory || d.key, value: d.value || d.amount || d.cost })) : [{ name: '暂无', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) {}
}

const loadInsuranceChart = async () => {
  try {
    const data = await getInsuranceAnalysis()
    if (!insuranceChartRef.value) return; if (insuranceChart) insuranceChart.dispose()
    insuranceChart = echarts.init(insuranceChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const names = list.map(d => d.name || d.insuranceType || d.category || d.key)
    const vals = list.map(d => d.value || d.amount || d.count)
    insuranceChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: names.length ? names : ['暂无'] },
      yAxis: { type: 'value', name: '金额(元)' },
      series: [{ type: 'bar', data: vals.length ? vals : [0], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' } }]
    })
  } catch (e) {}
}

const getList = async () => {
  loading.value = true
  try { const res = await listCost(queryParams); costList.value = res.rows || res.records || res.list || []; total.value = res.total || costList.value.length } catch (e) { costList.value = []; total.value = 0 }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryParams.costCategory = ''; handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加费用记录'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改费用记录'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try { if (form.value.id) await updateCost(form.value); else await addCost(form.value); ElMessage.success('操作成功'); dialogVisible.value = false; getList() } catch (e) { ElMessage.error('操作失败') }
  })
}
const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(async () => { try { await delCost(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') } }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); loadCompositionChart(); loadInsuranceChart(); window.addEventListener('resize', () => { compositionChart?.resize(); insuranceChart?.resize() }) })
onBeforeUnmount(() => { compositionChart?.dispose(); insuranceChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
