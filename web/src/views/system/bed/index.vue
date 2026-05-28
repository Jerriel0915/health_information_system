<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>床位按科室分布</template><div ref="deptChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>床位使用率趋势</template><div ref="trendChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="机构ID" prop="orgId"><el-input v-model="queryParams.orgId" placeholder="请输入机构ID" clearable /></el-form-item>
      <el-form-item label="科室类型" prop="deptType"><el-input v-model="queryParams.deptType" placeholder="请输入科室类型" clearable /></el-form-item>
      <el-form-item label="统计年份" prop="statYear"><el-input v-model="queryParams.statYear" placeholder="请输入统计年份" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="bedList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="机构ID" prop="orgId" />
      <el-table-column label="床位总数" prop="bedCount" />
      <el-table-column label="实际开放床位" prop="actualBedCount" />
      <el-table-column label="使用床位" prop="usedBedCount" />
      <el-table-column label="使用率(%)" prop="bedUsageRate" />
      <el-table-column label="科室类型" prop="deptType" />
      <el-table-column label="统计年份" prop="statYear" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="机构ID" prop="orgId"><el-input v-model="form.orgId" /></el-form-item>
        <el-form-item label="床位总数" prop="bedCount"><el-input v-model="form.bedCount" /></el-form-item>
        <el-form-item label="实际开放床位" prop="actualBedCount"><el-input v-model="form.actualBedCount" /></el-form-item>
        <el-form-item label="使用床位" prop="usedBedCount"><el-input v-model="form.usedBedCount" /></el-form-item>
        <el-form-item label="床位使用率" prop="bedUsageRate"><el-input v-model="form.bedUsageRate" /></el-form-item>
        <el-form-item label="科室类型" prop="deptType"><el-input v-model="form.deptType" /></el-form-item>
        <el-form-item label="统计年份" prop="statYear"><el-input v-model="form.statYear" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listBed, addBed, updateBed, delBed, getBedSummary, getBedDeptDistribution, getBedUsageRate } from '@/api/system/bed'

const statData = ref([{ title: '编制床位', value: 0, unit: '张' }, { title: '实有床位', value: 0, unit: '张' }, { title: '使用床位', value: 0, unit: '张' }, { title: '床位使用率', value: 0, unit: '%' }])
const deptChartRef = ref(null), trendChartRef = ref(null)
let deptChart = null, trendChart = null
const loading = ref(false), bedList = ref([]), total = ref(0), showSearch = ref(true), single = ref(true), multiple = ref(true), ids = ref([]), dialogVisible = ref(false), dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, orgId: '', deptType: '', statYear: '' })
const form = ref({}), formRef = ref(null), queryFormRef = ref(null)
const rules = { orgId: [{ required: true, message: '请填写机构ID' }] }

const formatNumber = (val) => { if (!val && val !== 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '\u4e07'; return val.toLocaleString() }

const loadSummary = async () => {
  try { const d = await getBedSummary(); statData.value[0].value = d.bedCount || d.totalBeds || 0; statData.value[1].value = d.actualBedCount || d.actualBeds || 0; statData.value[2].value = d.usedBedCount || d.usedBeds || 0; statData.value[3].value = d.bedUsageRate || d.usageRate || 0 } catch (e) {}
}

const loadDeptChart = async () => {
  try {
    const data = await getBedDeptDistribution()
    if (!deptChartRef.value) return; if (deptChart) deptChart.dispose()
    deptChart = echarts.init(deptChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    deptChart.setOption({
      tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.deptType || d.key, value: d.value || d.count })) : [{ name: '暂无', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) {}
}

const loadTrendChart = async () => {
  try {
    const data = await getBedUsageRate()
    if (!trendChartRef.value) return; if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const years = list.map(d => d.year || d.time || d.date || d.key)
    const vals = list.map(d => d.value || d.rate || d.usageRate)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: years.length ? years : ['暂无'] },
      yAxis: { type: 'value', name: '使用率(%)', max: 100 },
      series: [{ type: 'line', data: vals.length ? vals : [0], smooth: true, lineStyle: { color: '#409EFF', width: 3 }, areaStyle: { opacity: 0.3 }, symbol: 'circle', symbolSize: 8 }]
    })
  } catch (e) {}
}

const getList = async () => {
  loading.value = true
  try { const res = await listBed(queryParams); bedList.value = res.rows || res.records || res.list || []; total.value = res.total || bedList.value.length } catch (e) { bedList.value = []; total.value = 0 }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加床位数据'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改床位数据'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try { if (form.value.id) await updateBed(form.value); else await addBed(form.value); ElMessage.success('操作成功'); dialogVisible.value = false; getList() } catch (e) { ElMessage.error('操作失败') }
  })
}
const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(async () => { try { await delBed(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') } }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); loadDeptChart(); loadTrendChart(); window.addEventListener('resize', () => { deptChart?.resize(); trendChart?.resize() }) })
onBeforeUnmount(() => { deptChart?.dispose(); trendChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
