<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ item.value }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <template #header>区域层级结构<el-button text size="small" style="float: right;" @click="renderTreeChart">刷新</el-button></template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listRegion, updateRegion, delRegion, getRegionSummary, getRegionTree } from '@/api/system/region'

const stats = ref([])

const loadStats = async () => {
  try {
    const d = await getRegionSummary()
    stats.value = [
      { title: '区域总数', value: d.totalCount || d.total || 0, unit: '个' },
      { title: '市级区域', value: d.cityCount || d.city || 0, unit: '个' },
      { title: '区县级', value: d.districtCount || d.district || 0, unit: '个' },
      { title: '乡镇级', value: d.townCount || d.town || 0, unit: '个' }
    ]
  } catch(e) { stats.value = [{title:'区域总数',value:0,unit:'个'},{title:'市级区域',value:0,unit:'个'},{title:'区县级',value:0,unit:'个'},{title:'乡镇级',value:0,unit:'个'}] }
}

const chartRefs = { tree: ref(null) }
const charts = {}

const renderCharts = {
  tree: async () => {
    try {
      const data = await getRegionTree()
      if (!chartRefs.tree.value) return
      if (charts.tree) charts.tree.dispose()
      charts.tree = echarts.init(chartRefs.tree.value)
      const list = Array.isArray(data) ? data : (data.records || data.list || [])
      charts.tree.setOption({
        tooltip: { trigger: 'item', triggerOn: 'mousemove' },
        series: [{ type: 'tree', data: list.length ? list : [{name: '暂无数据'}], top: '1%', left: '15%', bottom: '1%', right: '20%', symbolSize: 7, label: { position: 'left', verticalAlign: 'middle', align: 'right', fontSize: 9 }, leaves: { label: { position: 'right', verticalAlign: 'middle', align: 'left' } }, expandAndCollapse: true, animationDuration: 550 }]
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
const getList = async () => { loading.value = true; try { const res = await listRegion(queryParams); dataList.value = res.rows || res.records || res.list || []; total.value = res.total || dataList.value.length } catch(e) { dataList.value = []; total.value = 0 }; loading.value = false }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length != 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = "添加"; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = "修改"; dialogVisible.value = true }
const submitForm = async () => { formRef.value?.validate(async val => { if (!val) return; try { if (form.value.id) await updateRegion(form.value); else await addRegion(form.value); ElMessage.success("操作成功"); dialogVisible.value = false; getList() } catch(e) { ElMessage.error("操作失败") } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(","); ElMessageBox.confirm("确认删除？").then(async () => { try { await delRegion(id); ElMessage.success("删除成功"); getList() } catch(e) { ElMessage.error("删除失败") } }).catch(() => {}) }

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
.chart-card { margin-bottom: 20px; }
.mb8 { margin-bottom: 8px; }
</style>