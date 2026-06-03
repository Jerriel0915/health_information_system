<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="(item, idx) in stats" :key="idx">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">{{ item.title }}</div><div class="stat-value">{{ item.value }}<span class="unit">{{ item.unit }}</span></div></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>机构类型分布</template>
          <div ref="typeChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>机构等级分布</template>
          <div ref="levelChartRef" style="height: 300px;"></div>
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

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="机构编码" prop="orgCode" />
      <el-table-column label="机构名称" prop="orgName" />
      <el-table-column label="机构类型" prop="orgType" />
      <el-table-column label="机构等级" prop="orgLevel" />
      <el-table-column label="所属区域" prop="regionName" show-overflow-tooltip />
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="机构编码" prop="orgCode"><el-input v-model="form.orgCode" placeholder="请输入机构编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="机构名称" prop="orgName"><el-input v-model="form.orgName" placeholder="请输入机构名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="机构类型" prop="orgType"><el-select v-model="form.orgType" placeholder="请选择机构类型" style="width: 100%;"><el-option v-for="item in orgTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="机构等级" prop="orgLevel"><el-select v-model="form.orgLevel" placeholder="请选择机构等级" style="width: 100%;" clearable><el-option v-for="item in orgLevelOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="所属区域" prop="regionId"><el-select v-model="form.regionId" placeholder="请选择所属区域" style="width: 100%;" filterable><el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入联系电话" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" placeholder="请输入地址" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { listInstitution, addInstitution, updateInstitution, delInstitution, getInstitutionSummary, getInstitutionTypeDistribution, getInstitutionLevelDistribution } from '@/api/system/institution'
import { listRegion } from '@/api/system/region'

const stats = ref([])
const list = ref([])
const total = ref(0)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, orgName: '' })
const form = ref({})
const formRef = ref(null)
const submitLoading = ref(false)

const orgTypeOptions = [
  { value: '综合医院', label: '综合医院' },
  { value: '专科医院', label: '专科医院' },
  { value: '基层机构', label: '基层机构' },
  { value: '专业公共卫生机构', label: '专业公共卫生机构' },
  { value: '其他', label: '其他' }
]

const orgLevelOptions = [
  { value: '三级甲等', label: '三级甲等' },
  { value: '三级乙等', label: '三级乙等' },
  { value: '三级丙等', label: '三级丙等' },
  { value: '二级甲等', label: '二级甲等' },
  { value: '二级乙等', label: '二级乙等' },
  { value: '二级丙等', label: '二级丙等' },
  { value: '一级甲等', label: '一级甲等' },
  { value: '一级乙等', label: '一级乙等' },
  { value: '一级丙等', label: '一级丙等' },
  { value: '未定级', label: '未定级' }
]

const regionOptions = ref([])

const rules = {
  orgCode: [{ required: true, message: '请填写机构编码' }],
  orgName: [{ required: true, message: '请填写机构名称' }],
  orgType: [{ required: true, message: '请选择机构类型' }],
  regionId: [{ required: true, message: '请选择所属区域' }]
}

const typeChartRef = ref(null)
const levelChartRef = ref(null)
let typeChart = null
let levelChart = null

const loadSummary = async () => {
  try {
    const res = await getInstitutionSummary()
    if (res.code === 200 && res.data) {
      const d = res.data
      const titleMap = { totalInstitutions: '机构总数', typeCount: '机构类型', levelCount: '机构等级', activeCount: '启用机构' }
      const items = []
      for (const [key, value] of Object.entries(d)) {
        const unit = (key === 'totalInstitutions' || key === 'activeCount') ? '家' : '种'
        items.push({ key, title: titleMap[key] || key, value, unit })
      }
      stats.value = items.slice(0, 4)
    }
  } catch (e) { console.error('加载机构统计失败', e) }
}

const renderTypeChart = async () => {
  if (!typeChartRef.value) return
  try {
    const res = await getInstitutionTypeDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: ['32%', '55%'], data: data.length ? data : [{ name: '暂无数据', value: 1 }], label: { show: true, formatter: '{b}: {d}%' } }]
    })
  } catch (e) { console.error('加载机构类型分布失败', e) }
}

const renderLevelChart = async () => {
  if (!levelChartRef.value) return
  try {
    const res = await getInstitutionLevelDistribution()
    const data = res.code === 200 ? (res.data || []) : []
    if (levelChart) levelChart.dispose()
    levelChart = echarts.init(levelChartRef.value)
    levelChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { top: 20, bottom: 30, left: 50, right: 20 },
      xAxis: { type: 'category', data: data.map(d => d.name) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: data.map(d => d.value) || [], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
    })
  } catch (e) { console.error('加载机构等级分布失败', e) }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listInstitution(queryParams.value)
    if (res.code === 200) {
      list.value = res.rows || []
      total.value = res.total || 0
    }
  } catch (e) { console.error('查询机构列表失败', e) }
  loading.value = false
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => { queryParams.value.orgName = ''; queryParams.value.pageNum = 1; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }

const loadRegionOptions = async () => {
  try {
    const res = await listRegion({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      regionOptions.value = (res.rows || []).filter(r => Number(r.regionLevel) === 3)
    }
  } catch (e) { console.error('加载区域列表失败', e) }
}

const handleAdd = async () => {
  form.value = { isActive: 1 }
  dialogTitle.value = '添加机构'
  dialogVisible.value = true
  await loadRegionOptions()
}

const handleUpdate = async (row) => {
  form.value = {
    id: row.id,
    orgCode: row.orgCode,
    orgName: row.orgName,
    orgType: row.orgType,
    orgLevel: row.orgLevel || '',
    regionId: Number(row.regionId),
    address: row.address,
    contactPhone: row.contactPhone,
    isActive: row.isActive != null ? Number(row.isActive) : 1
  }
  dialogTitle.value = '修改机构'
  dialogVisible.value = true
  await loadRegionOptions()
}

const submitForm = async () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form.value }
      if (payload.orgLevel === '') delete payload.orgLevel
      const res = payload.id ? await updateInstitution(payload) : await addInstitution(payload)
      if (res.code === 200) {
        ElMessage.success('操作成功')
          await renderTypeChart(), renderLevelChart()
        dialogVisible.value = false
        getList()
      }
    } catch (e) { console.error('保存机构失败', e) }
    finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  const delIds = row?.id || ids.value.join(',')
  try {
    await ElMessageBox.confirm('确认删除？')
    const res = await delInstitution(delIds)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (e) { if (e !== 'cancel') console.error('删除机构失败', e) }
}

onMounted(async () => {
  await loadSummary()
  await Promise.all([renderTypeChart(), renderLevelChart()])
  getList()
  window.addEventListener('resize', () => { typeChart?.resize(); levelChart?.resize() })
})
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
