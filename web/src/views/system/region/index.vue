<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ item.value }}<span class="unit">{{ item.unit }}</span></div></div></el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <template #header>区域层级结构</template>
      <div ref="treeChartRef" style="height: 450px;"></div>
    </el-card>

    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="区域名称" prop="regionName"><el-input v-model="queryParams.regionName" placeholder="请输入区域名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>

    <el-table v-loading="loading" :data="regionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" /><el-table-column label="序号" prop="id" width="80" /><el-table-column label="区域编码" prop="regionCode" /><el-table-column label="区域名称" prop="regionName" /><el-table-column label="区域级别" prop="regionLevel"><template #default="{ row }"><el-tag :type="row.regionLevel === 1 || row.regionLevel === '1' ? ''danger'' : row.regionLevel === 2 || row.regionLevel === '2' ? ''warning'' : ''success''">{{ row.regionLevel == 1 ? ''省级'' : row.regionLevel == 2 ? ''市级'' : ''区县级'' }}</el-tag></template></el-table-column><el-table-column label="父级ID" prop="parentId" /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="区域编码" prop="regionCode"><el-input v-model="form.regionCode" /></el-form-item>
        <el-form-item label="区域名称" prop="regionName"><el-input v-model="form.regionName" /></el-form-item>
        <el-form-item label="区域级别" prop="regionLevel"><el-select v-model="form.regionLevel"><el-option label="省级" :value="1" /><el-option label="市级" :value="2" /><el-option label="区县级" :value="3" /></el-select></el-form-item>
        <el-form-item label="父级ID" prop="parentId"><el-input v-model="form.parentId" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listRegion, addRegion, updateRegion, delRegion, getRegionTree, getRegionSummary } from '@/api/system/region'

const statData = ref([{ title: '区域总数', value: 0, unit: '个' }, { title: '省级区域', value: 0, unit: '个' }, { title: '市级区域', value: 0, unit: '个' }, { title: '区县级', value: 0, unit: '个' }])
const treeChartRef = ref(null); let treeChart = null
const loading = ref(false), regionList = ref([]), total = ref(0), showSearch = ref(true), single = ref(true), multiple = ref(true), ids = ref([]), dialogVisible = ref(false), dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, regionName: '' })
const form = ref({}), formRef = ref(null), queryFormRef = ref(null)
const rules = { regionCode: [{ required: true, message: '请填写区域编码' }], regionName: [{ required: true, message: '请填写区域名称' }] }

const loadSummary = async () => {
  try { const d = await getRegionSummary(); statData.value[0].value = d.totalCount || d.total || 0; statData.value[1].value = d.provinceCount || 0; statData.value[2].value = d.cityCount || 0; statData.value[3].value = d.districtCount || 0 } catch (e) {}
}

const renderTreeChart = async () => {
  try {
    const data = await getRegionTree()
    if (!treeChartRef.value) return; if (treeChart) treeChart.dispose()
    treeChart = echarts.init(treeChartRef.value)
    const treeData = Array.isArray(data) ? data : (data.tree || data.rows || [data])
    treeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'tree', data: treeData.length ? treeData : [{ name: '暂无数据' }], left: '2%', right: '2%', top: '10%', bottom: '10%', symbol: 'circle', symbolSize: 12, label: { position: 'left', fontSize: 12 }, expandAndCollapse: true, initialTreeDepth: 2 }]
    })
  } catch (e) {
    if (treeChartRef.value) { if (treeChart) treeChart.dispose(); treeChart = echarts.init(treeChartRef.value); treeChart.setOption({ series: [{ type: 'tree', data: [{ name: '暂无数据' }] }] }) }
  }
}

const getList = async () => {
  loading.value = true
  try { const res = await listRegion(queryParams); regionList.value = res.rows || res.records || res.list || []; total.value = res.total || regionList.value.length } catch (e) { regionList.value = []; total.value = 0 }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加区域'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改区域'; dialogVisible.value = true }
const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try { if (form.value.id) await updateRegion(form.value); else await addRegion(form.value); ElMessage.success('操作成功'); dialogVisible.value = false; getList() } catch (e) { ElMessage.error('操作失败') }
  })
}
const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(async () => { try { await delRegion(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') } }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); renderTreeChart(); window.addEventListener('resize', () => treeChart?.resize()) })
onBeforeUnmount(() => { treeChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
