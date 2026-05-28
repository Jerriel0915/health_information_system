<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="item in statData" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ formatNumber(item.value) }}<span class="unit">{{ item.unit }}</span></div></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>机构类型分布</template>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>机构等级分布</template>
          <div ref="levelChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="机构名称"><el-input v-model="queryParams.orgName" placeholder="请输入机构名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="institutionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="机构编码" prop="orgCode" />
      <el-table-column label="机构名称" prop="orgName" />
      <el-table-column label="机构类型" prop="orgType" />
      <el-table-column label="机构等级" prop="orgLevel" />
      <el-table-column label="地址" prop="address" show-overflow-tooltip />
      <el-table-column label="联系电话" prop="contactPhone" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row><el-col :span="12"><el-form-item label="机构编码" prop="orgCode"><el-input v-model="form.orgCode" /></el-form-item></el-col><el-col :span="12"><el-form-item label="机构名称" prop="orgName"><el-input v-model="form.orgName" /></el-form-item></el-col></el-row>
        <el-row><el-col :span="12"><el-form-item label="机构类型" prop="orgType"><el-input v-model="form.orgType" /></el-form-item></el-col><el-col :span="12"><el-form-item label="机构等级" prop="orgLevel"><el-input v-model="form.orgLevel" /></el-form-item></el-col></el-row>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listInstitution, addInstitution, updateInstitution, delInstitution, getInstitutionSummary, getInstitutionTypeDistribution, getInstitutionLevelDistribution } from '@/api/system/institution'

const statData = ref([{ title: '机构总数', value: 0, unit: '家' }, { title: '综合医院', value: 0, unit: '家' }, { title: '专科医院', value: 0, unit: '家' }, { title: '基层机构', value: 0, unit: '家' }])
const typeChartRef = ref(null)
const levelChartRef = ref(null)
let typeChart = null, levelChart = null

const loading = ref(false)
const institutionList = ref([])
const total = ref(0)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, orgName: '' })
const form = ref({})
const formRef = ref(null)
const rules = { orgCode: [{ required: true, message: '请填写机构编码' }], orgName: [{ required: true, message: '请填写机构名称' }] }

const formatNumber = (val) => { if (!val && val !== 0) return 0; if (val >= 10000) return (val / 10000).toFixed(1) + '\u4e07'; return val.toLocaleString() }

const loadSummary = async () => {
  try {
    const data = await getInstitutionSummary()
    const list = Array.isArray(data) ? data : []
    const totalCount = list.find(d => d.name === 'total' || d.key === 'total')?.value || list.length || 0
    statData.value[0].value = data.totalCount || data.total || totalCount
    statData.value[1].value = data.generalCount || data.comprehensive || 0
    statData.value[2].value = data.specialistCount || data.specialist || 0
    statData.value[3].value = data.primaryCount || data.primary || 0
  } catch (e) {}
}

const loadTypeChart = async () => {
  try {
    const data = await getInstitutionTypeDistribution()
    if (!typeChartRef.value) return
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    typeChart.setOption({
      tooltip: { trigger: 'item' }, legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: list.length ? list.map(d => ({ name: d.name || d.typeName || d.key, value: d.value || d.count })) : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) {}
}

const loadLevelChart = async () => {
  try {
    const data = await getInstitutionLevelDistribution()
    if (!levelChartRef.value) return
    if (levelChart) levelChart.dispose()
    levelChart = echarts.init(levelChartRef.value)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    const names = list.map(d => d.name || d.level || d.key)
    const values = list.map(d => d.value || d.count)
    levelChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: names.length ? names : ['暂无'] },
      yAxis: { type: 'value', name: '数量' },
      series: [{ type: 'bar', data: values.length ? values : [0], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
    })
  } catch (e) {}
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listInstitution(queryParams)
    institutionList.value = res.rows || res.records || res.list || []
    total.value = res.total || institutionList.value.length
  } catch (e) {
    institutionList.value = []
    total.value = 0
  }
  loading.value = false
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { queryParams.orgName = ''; handleQuery() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加机构'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = { ...(row || {}) }; dialogTitle.value = '修改机构'; dialogVisible.value = true }

const submitForm = async () => {
  formRef.value?.validate(async valid => {
    if (!valid) return
    try {
      if (form.value.id) await updateInstitution(form.value)
      else await addInstitution(form.value)
      ElMessage.success('操作成功')
      dialogVisible.value = false
      getList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

const handleDelete = (row) => {
  const delIds = row?.id || ids.value.join(',')
  ElMessageBox.confirm('确认删除？').then(async () => {
    try { await delInstitution(delIds); ElMessage.success('删除成功'); getList() } catch (e) { ElMessage.error('删除失败') }
  }).catch(() => {})
}

onMounted(() => { getList(); loadSummary(); loadTypeChart(); loadLevelChart(); window.addEventListener('resize', () => { typeChart?.resize(); levelChart?.resize() }) })
onBeforeUnmount(() => { typeChart?.dispose(); levelChart?.dispose(); window.removeEventListener('resize', () => {}) })
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
