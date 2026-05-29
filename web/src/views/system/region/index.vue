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

    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col><el-col :span="1.5"><el-button type="success" plain :disabled="single" @click="handleUpdate">修改</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain :disabled="multiple" @click="handleDelete">删除</el-button></el-col><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>

    <el-table v-loading="loading" :data="regionList" @selection-change="handleSelectionChange">
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const stats = ref([{ title: '区域总数', value: 15, unit: '个' }, { title: '省级区域', value: 1, unit: '个' }, { title: '市级区域', value: 5, unit: '个' }, { title: '区县级', value: 9, unit: '个' }])

const treeChartRef = ref(null); let treeChart = null
const renderTreeChart = () => { if (!treeChartRef.value) return; if (treeChart) treeChart.dispose(); treeChart = echarts.init(treeChartRef.value); treeChart.setOption({ tooltip: { trigger: 'item' }, series: [{ type: 'tree', data: [{ name: '本省', children: [{ name: 'A市', children: [{ name: 'A1区' }, { name: 'A2区' }] }, { name: 'B市', children: [{ name: 'B1区' }, { name: 'B2区' }] }, { name: 'C市' }] }], left: '2%', right: '2%', top: '10%', bottom: '10%', symbol: 'circle', symbolSize: 12, label: { position: 'left', fontSize: 12 }, expandAndCollapse: true, initialTreeDepth: 2 }] }) }

const loading = ref(false); const regionList = ref([]); const total = ref(0); const showSearch = ref(true); const single = ref(true); const multiple = ref(true); const ids = ref([]); const dialogVisible = ref(false); const dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, regionName: '' })
const form = ref({}); const formRef = ref(null); const queryFormRef = ref(null)
const rules = { regionCode: [{ required: true, message: '请填写区域编码' }], regionName: [{ required: true, message: '请填写区域名称' }] }

const getList = () => { loading.value = true; setTimeout(() => { regionList.value = [{ id: 1, regionCode: '001', regionName: 'A市', regionLevel: '2', parentId: '0' }]; total.value = 1; loading.value = false }, 500) }
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加区域'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = '修改区域'; dialogVisible.value = true }
const submitForm = () => { formRef.value?.validate(valid => { if (valid) { ElMessage.success('操作成功'); dialogVisible.value = false; getList() } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(() => { ElMessage.success('删除成功'); getList() }).catch(() => {}) }

onMounted(() => { getList(); renderTreeChart(); window.addEventListener('resize', () => treeChart?.resize()) })
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