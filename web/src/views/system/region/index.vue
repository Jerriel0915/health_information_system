<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ item.value }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <template #header>区域层级结构<el-button text size="small" style="float: right;" @click="renderTreeChart">刷新</el-button></template>
      <div ref="treeChartRef" style="height: 450px;"></div>
    </el-card>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="区域名称" prop="regionName"><el-input v-model="queryParams.regionName" placeholder="请输入区域名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="区域编码" prop="regionCode" /><el-table-column label="区域名称" prop="regionName" /><el-table-column label="区域级别" prop="regionLevel"><template #default="{ row }"><el-tag :type="row.regionLevel === '1' ? 'danger' : row.regionLevel === '2' ? 'warning' : 'success'">{{ row.regionLevel === '1' ? '省级' : row.regionLevel === '2' ? '市级' : '区县级' }}</el-tag></template></el-table-column><el-table-column label="父级ID" prop="parentId" /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="区域编码" prop="regionCode"><el-input v-model="form.regionCode" /></el-form-item>
        <el-form-item label="区域名称" prop="regionName"><el-input v-model="form.regionName" /></el-form-item>
        <el-form-item label="区域级别" prop="regionLevel"><el-select v-model="form.regionLevel"><el-option label="省级" value="1" /><el-option label="市级" value="2" /><el-option label="区县级" value="3" /></el-select></el-form-item>
        <el-form-item label="父级ID" prop="parentId"><el-input v-model="form.parentId" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
const showSearch = ref(true)
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listRegion, addRegion, updateRegion, delRegion, getRegionSummary } from '@/api/system/region'

// 统计卡片
const stats = ref([])

// 列表
const list = ref([])
const total = ref(0)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10 })
const form = ref({})
const formRef = ref(null)
const rules = {}

// 加载统计卡片
const loadSummary = async () => {
  try {
    const res = await getRegionSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const items = []
      const titleMap = {
        totalRegions: '区域总数', levelCount: '行政级别数'
      }
      for (const [key, value] of Object.entries(d)) {
        items.push({ key, title: titleMap[key] || key, value: value, unit: '' })
      }
      stats.value = items.slice(0, 4)
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// CRUD
const getList = async () => {
  loading.value = true
  try {
    const res = await listRegion(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('查询列表失败', e)
  }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNum: 1, pageSize: 10 }; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row ? { ...row } : {}; dialogTitle.value = '修改'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = form.value.id ? await updateRegion(form.value) : await addRegion(form.value)
        if (res.code === 200) {
          ElMessage.success('操作成功')
          dialogVisible.value = false
          getList()
        }
      } catch (e) {
        console.error('保存失败', e)
      }
    }
  })
}
const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delRegion(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

import { getRegionTree } from '@/api/system/region'

const treeChartRef = ref(null)

let treeChart = null


// 将扁平区域列表转为树形结构
const buildTree = (items) => {
  const map = {}
  const roots = []
  items.forEach(item => { map[item.id] = { ...item, children: [] } })
  items.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else if (!item.parentId) {
      roots.push(map[item.id])
    }
  })
  return roots
}

const renderChart1 = async () => {
  if (!treeChartRef.value) return
  try {
    const res = await getRegionTree()
    const flatData = res.code === 200 ? (res.data || []) : []
    const treeData = flatData.length ? buildTree(flatData) : [{ name: '暂无数据' }]
    if (treeChart) treeChart.dispose()
    treeChart = echarts.init(treeChartRef.value)
    treeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'tree',
        data: treeData.length ? treeData : [{ name: '暂无数据' }],
        left: '2%', right: '2%', top: '10%', bottom: '10%',
        symbol: 'circle', symbolSize: 12,
        label: { position: 'left', fontSize: 12 },
        expandAndCollapse: true, initialTreeDepth: 2
      }]
    })
  } catch (e) { console.error('加载区域树失败', e) }
}

const renderChart2 = async () => {}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { treeChart?.resize() })
})
onBeforeUnmount(() => { treeChart?.dispose() })
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



