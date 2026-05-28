<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>服务类型分布<el-button text size="small" style="float: right;" @click="renderTypeChart">刷新</el-button></template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listService, updateService, delService, getServiceSummary, getServiceTypeDistribution, getServiceTrend } from '@/api/system/service'

const stats = ref([])

const loadStats = async () => {
  try {
    const d = await getServiceSummary()
    stats.value = [
      { title: '服务总量', value: d.totalCount || d.total || 0, unit: '人次' },
      { title: '门诊服务', value: d.outpatientCount || d.outpatient || 0, unit: '人次' },
      { title: '住院服务', value: d.inpatientCount || d.inpatient || 0, unit: '人次' },
      { title: '平均住院天数', value: d.avgDays || d.averageDays || 0, unit: '天' }
    ]
  } catch(e) { stats.value = [{title:'服务总量',value:0,unit:'人次'},{title:'门诊服务',value:0,unit:'人次'},{title:'住院服务',value:0,unit:'人次'},{title:'平均住院天数',value:0,unit:'天'}] }
}

const chartRefs = { type: ref(null), trend: ref(null) }
const charts = {}

const renderCharts = {
  type: async () => {
    try {
      const data = await getServiceTypeDistribution()
      if (!chartRefs.type.value) return
      if (charts.type) charts.type.dispose()
      charts.type = echarts.init(chartRefs.type.value)
      const list = Array.isArray(data) ? data : (data.records || data.list || [])
      charts.type.setOption({
        tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
        series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.serviceType || d.key, value: d.value || d.count || 0 })) : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
      })
    } catch(e) {}
  },
  trend: async () => {
    try {
      const data = await getServiceTrend()
      if (!chartRefs.trend.value) return
      if (charts.trend) charts.trend.dispose()
      charts.trend = echarts.init(chartRefs.trend.value)
      const list = Array.isArray(data) ? data : (data.records || data.list || [])
      const years = list.map(d => d.year || d.time || d.date || d.key || '')
      const values = list.map(d => d.value || d.count || d.serviceCount || 0)
      charts.trend.setOption({
        tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: years.length ? years : ['暂无'], name: '年份' },
        yAxis: { type: 'value', name: '服务量（人次）' },
        series: [{ type: 'line', data: values.length ? values : [0], smooth: true, lineStyle: { color: '#409EFF', width: 3 }, areaStyle: { opacity: 0.3 } }]
      })
    } catch(e) {}
  }
}

const loading = ref(false), dataList = ref([]), total = ref(0)
const showSearch = ref(true), single = ref(true), multiple = ref(true), ids = ref([])
const dialogVisible = ref(false), dialogTitle = ref("")
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const form = ref({}), formRef = ref(null), queryFormRef = ref(null)
const rules = {}

const formatNumber = (val) => { if (!val && val != 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + "万"; return val.toLocaleString() }
const getList = async () => { loading.value = true; try { const res = await listService(queryParams); dataList.value = res.rows || res.records || res.list || []; total.value = res.total || dataList.value.length } catch(e) { dataList.value = []; total.value = 0 }; loading.value = false }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length != 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = "添加"; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = "修改"; dialogVisible.value = true }
const submitForm = async () => { formRef.value?.validate(async val => { if (!val) return; try { if (form.value.id) await updateService(form.value); else await addService(form.value); ElMessage.success("操作成功"); dialogVisible.value = false; getList() } catch(e) { ElMessage.error("操作失败") } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(","); ElMessageBox.confirm("确认删除？").then(async () => { try { await delService(id); ElMessage.success("删除成功"); getList() } catch(e) { ElMessage.error("删除失败") } }).catch(() => {}) }

const resizeAll = () => { Object.values(charts).forEach(c => c?.resize()) }
onMounted(() => { getList(); loadStats(); Object.values(renderCharts).forEach(fn => fn()); window.addEventListener("resize", resizeAll) })
onBeforeUnmount(() => { Object.values(charts).forEach(c => c?.dispose()) })
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