<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">人员总数</div><div class="stat-value">5,206<span class="unit">人</span></div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">医生人数</div><div class="stat-value">1,978<span class="unit">人</span></div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">护士人数</div><div class="stat-value">2,343<span class="unit">人</span></div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">医技人员</div><div class="stat-value">885<span class="unit">人</span></div></div></el-card></el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>职称分布</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listStaff, updateStaff, delStaff, getStaffSummary, getStaffJobTitleDistribution, getStaffEducationDistribution } from '@/api/system/staff'

const stats = ref([])

const loadStats = async () => {
  try {
    const d = await getStaffSummary()
    stats.value = [
      { title: '人员总数', value: d.totalCount || d.total || 0, unit: '人' },
      { title: '医生人数', value: d.doctorCount || d.doctors || 0, unit: '人' },
      { title: '护士人数', value: d.nurseCount || d.nurses || 0, unit: '人' },
      { title: '医技人员', value: d.techCount || d.technicians || 0, unit: '人' }
    ]
  } catch(e) { stats.value = [{title:'人员总数',value:0,unit:'人'},{title:'医生人数',value:0,unit:'人'},{title:'护士人数',value:0,unit:'人'},{title:'医技人员',value:0,unit:'人'}] }
}

const chartRefs = { jobTitle: ref(null), education: ref(null) }
const charts = {}

const renderCharts = {
  jobTitle: async () => {
    try {
      const data = await getStaffJobTitleDistribution()
      if (!chartRefs.jobTitle.value) return
      if (charts.jobTitle) charts.jobTitle.dispose()
      charts.jobTitle = echarts.init(chartRefs.jobTitle.value)
      const list = Array.isArray(data) ? data : (data.records || data.list || [])
      charts.jobTitle.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: { type: 'category', data: list.map(d => d.name || d.jobTitle || d.key), axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', name: '人数' },
        series: [{ type: 'bar', data: list.map(d => d.value || d.count || 0), itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' } }]
      })
    } catch(e) {}
  },
  education: async () => {
    try {
      const data = await getStaffEducationDistribution()
      if (!chartRefs.education.value) return
      if (charts.education) charts.education.dispose()
      charts.education = echarts.init(chartRefs.education.value)
      const list = Array.isArray(data) ? data : (data.records || data.list || [])
      charts.education.setOption({
        tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
        series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.education || d.key, value: d.value || d.count || 0 })) : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
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
const getList = async () => { loading.value = true; try { const res = await listStaff(queryParams); dataList.value = res.rows || res.records || res.list || []; total.value = res.total || dataList.value.length } catch(e) { dataList.value = []; total.value = 0 }; loading.value = false }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length != 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = "添加"; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = "修改"; dialogVisible.value = true }
const submitForm = async () => { formRef.value?.validate(async val => { if (!val) return; try { if (form.value.id) await updateStaff(form.value); else await addStaff(form.value); ElMessage.success("操作成功"); dialogVisible.value = false; getList() } catch(e) { ElMessage.error("操作失败") } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(","); ElMessageBox.confirm("确认删除？").then(async () => { try { await delStaff(id); ElMessage.success("删除成功"); getList() } catch(e) { ElMessage.error("删除失败") } }).catch(() => {}) }

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