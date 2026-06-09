<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards"><el-col :span="6" v-for="stat in stats" :key="stat.key"><el-card shadow="hover" class="stat-card"><div class="stat-card-inner"><div class="stat-title">{{ stat.title }}</div><div class="stat-value">{{ stat.value }}<span class="unit">{{ stat.unit }}</span></div></div></el-card></el-col></el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12"><el-card shadow="hover"><template #header>职称分布</template><div ref="jobTitleChartRef" style="height: 300px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>学历分布</template><div ref="educationChartRef" style="height: 300px;"></div></el-card></el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="姓名"><el-input v-model="queryParams.staffName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="人员编号" prop="staffCode" />
      <el-table-column label="姓名" prop="staffName" />
      <el-table-column label="性别"><template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '' }}</template></el-table-column>
      <el-table-column label="所属机构" prop="orgName" show-overflow-tooltip />
      <el-table-column label="科室" prop="department" />
      <el-table-column label="职称" prop="jobTitle" />
      <el-table-column label="学历" prop="education" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="人员编号" prop="staffCode"><el-input v-model="form.staffCode" placeholder="请输入人员编号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="staffName"><el-input v-model="form.staffName" placeholder="请输入姓名" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%;"><el-option :label="'男'" :value="1" /><el-option :label="'女'" :value="2" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属机构" prop="orgId"><el-select v-model="form.orgId" placeholder="请选择所属机构" style="width: 100%;" filterable><el-option v-for="item in orgOptions" :key="item.id" :label="item.orgName" :value="item.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="科室"><el-input v-model="form.department" placeholder="请输入科室" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="职称"><el-input v-model="form.jobTitle" placeholder="请输入职称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="学历"><el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;"><el-option label="博士" value="博士" /><el-option label="硕士" value="硕士" /><el-option label="本科" value="本科" /><el-option label="大专" value="大专" /><el-option label="其他" value="其他" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="人员状态" prop="isActive"><el-select v-model="form.isActive" placeholder="请选择状态" style="width: 100%;"><el-option label="在职" :value="1" /><el-option label="离职" :value="0" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listStaff, addStaff, updateStaff, delStaff, getStaffSummary, getStaffJobTitleDistribution, getStaffEducationDistribution } from '@/api/system/staff'
import { listInstitution } from '@/api/system/institution'

const stats = ref([])
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
const submitLoading = ref(false)
const orgOptions = ref([])
const rules = { staffName: [{ required: true, message: '请填写姓名' }] }

const loadSummary = async () => {
  try {
    const res = await getStaffSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const titleMap = { totalStaff: '总人员数', titleCount: '职称类别', categoryCount: '职业类别', deptCount: '科室数量', activeCount: '在职人数' }
      const items = []
      for (const [key, value] of Object.entries(d)) {
        items.push({ key, title: titleMap[key] || key, value: value, unit: '' })
      }
      stats.value = items.slice(0, 4)
    }
  } catch (e) { console.error('加载统计失败', e) }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listStaff(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) { console.error('查询列表失败', e) }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNum: 1, pageSize: 10 }; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }

const loadOrgOptions = async () => {
  try {
    const res = await listInstitution({ pageNum: 1, pageSize: 200 })
    if (res.code === 200) {
      orgOptions.value = res.rows || []
    }
  } catch (e) { console.error('加载机构列表失败', e) }
}

const handleAdd = async () => {
  form.value = { isActive: 1 }
  dialogTitle.value = '添加人员'
  dialogVisible.value = true
  await loadOrgOptions()
}

const handleUpdate = async (row) => {
  form.value = {
    id: row.id,
    staffCode: row.staffCode || '',
    staffName: row.staffName,
    gender: row.gender != null ? Number(row.gender) : null,
    orgId: row.orgId != null ? Number(row.orgId) : null,
    department: row.department || '',
    jobTitle: row.jobTitle || '',
    education: row.education || '',
    isActive: row.isActive != null ? Number(row.isActive) : 1
  }
  dialogTitle.value = '修改人员'
  dialogVisible.value = true
  await loadOrgOptions()
}

const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form.value }
      const res = payload.id ? await updateStaff(payload) : await addStaff(payload)
      if (res.code === 200) {
        ElMessage.success('操作成功')
          await renderChart1(), renderChart2()
        dialogVisible.value = false
        getList()
      }
    } catch (e) { console.error('保存失败', e) }
    finally { submitLoading.value = false }
  })
}
const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delStaff(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) { if (e !== 'cancel') console.error('删除失败', e) }
}

const jobTitleChartRef = ref(null)
const educationChartRef = ref(null)
let jobTitleChart = null
let educationChart = null

const renderChart1 = async () => {
  if (!jobTitleChartRef.value) return
  try {
    const res = await getStaffJobTitleDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (jobTitleChart) jobTitleChart.dispose()
    jobTitleChart = echarts.init(jobTitleChartRef.value)
    jobTitleChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.name) || [] },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: data.map(d => d.value) || [], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
    })
  } catch (e) { console.error('加载职称分布失败', e) }
}

const renderChart2 = async () => {
  if (!educationChartRef.value) return
  try {
    const res = await getStaffEducationDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (educationChart) educationChart.dispose()
    educationChart = echarts.init(educationChartRef.value)
    educationChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: ['32%', '55%'], data: data.length ? data : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) { console.error('加载学历分布失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderChart1(), renderChart2()])
  getList()
  window.addEventListener('resize', () => { jobTitleChart?.resize(); educationChart?.resize() })
})
onBeforeUnmount(() => { jobTitleChart?.dispose(); educationChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
