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
      <el-col :span="12"><el-card shadow="hover"><template #header>职称分布</template><div ref="jobTitleChartRef" style="height: 320px;"></div></el-card></el-col>
      <el-col :span="12"><el-card shadow="hover"><template #header>学历分布</template><div ref="educationChartRef" style="height: 320px;"></div></el-card></el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="queryParams.staffName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="staffList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="序号" prop="id" width="80" />
      <el-table-column label="姓名" prop="staffName" />
      <el-table-column label="性别" prop="gender" />
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

    <!-- 对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="staffName"><el-input v-model="form.staffName" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
        <el-form-item label="科室"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.jobTitle" /></el-form-item>
        <el-form-item label="学历"><el-select v-model="form.education"><el-option label="博士" value="博士" /><el-option label="硕士" value="硕士" /><el-option label="本科" value="本科" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

// 全部数据（模拟后端数据库）
const allData = [
  { id: 1, staffName: '张明', gender: '男', department: '内科', jobTitle: '主任医师', education: '博士' },
  { id: 2, staffName: '李芳', gender: '女', department: '外科', jobTitle: '副主任医师', education: '硕士' },
  { id: 3, staffName: '王强', gender: '男', department: '儿科', jobTitle: '主治医师', education: '本科' },
  { id: 4, staffName: '赵丽', gender: '女', department: '护理部', jobTitle: '护士长', education: '本科' },
  { id: 5, staffName: '陈华', gender: '男', department: '放射科', jobTitle: '技师', education: '本科' },
  { id: 6, staffName: '王明', gender: '男', department: '内科', jobTitle: '主治医师', education: '硕士' },
  { id: 7, staffName: '刘芳', gender: '女', department: '儿科', jobTitle: '副主任医师', education: '博士' }
]

const staffList = ref([])
const total = ref(0)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, staffName: '' })
const form = ref({})
const formRef = ref(null)
const rules = { staffName: [{ required: true, message: '请填写姓名' }] }

// 获取列表（带搜索过滤）
const getList = () => {
  loading.value = true

  // 根据姓名筛选
  let filteredData = [...allData]
  if (queryParams.value.staffName) {
    filteredData = allData.filter(item =>
        item.staffName.includes(queryParams.value.staffName)
    )
  }

  // 分页处理
  const start = (queryParams.value.pageNum - 1) * queryParams.value.pageSize
  const end = start + queryParams.value.pageSize
  staffList.value = filteredData.slice(start, end)
  total.value = filteredData.length
  loading.value = false
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value.staffName = ''
  queryParams.value.pageNum = 1
  getList()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

const handleAdd = () => {
  form.value = {}
  dialogTitle.value = '添加人员'
  dialogVisible.value = true
}

const handleUpdate = (row) => {
  form.value = row ? { ...row } : {}
  dialogTitle.value = '修改人员'
  dialogVisible.value = true
}

const submitForm = () => {
  formRef.value?.validate(valid => {
    if (valid) {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      getList()
    }
  })
}

const handleDelete = (row) => {
  const id = row?.id || ids.value.join(',')
  ElMessageBox.confirm('确认删除？').then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 图表
const jobTitleChartRef = ref(null)
const educationChartRef = ref(null)
let jobTitleChart = null
let educationChart = null

const renderJobTitleChart = () => {
  if (!jobTitleChartRef.value) return
  if (jobTitleChart) jobTitleChart.dispose()
  jobTitleChart = echarts.init(jobTitleChartRef.value)
  jobTitleChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: ['主任医师', '副主任医师', '主治医师', '住院医师', '护士', '医技人员'], axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '人数' },
    series: [{ type: 'bar', data: [156, 423, 892, 507, 2343, 885], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
  })
}

const renderEducationChart = () => {
  if (!educationChartRef.value) return
  if (educationChart) educationChart.dispose()
  educationChart = echarts.init(educationChartRef.value)
  educationChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{ type: 'pie', radius: '55%', data: [{ name: '博士', value: 124 }, { name: '硕士', value: 867 }, { name: '本科', value: 2341 }, { name: '大专', value: 1456 }, { name: '中专', value: 418 }], label: { show: true, formatter: '{b}: {d}%' } }]
  })
}

onMounted(() => {
  getList()
  renderJobTitleChart()
  renderEducationChart()
  window.addEventListener('resize', () => {
    jobTitleChart?.resize()
    educationChart?.resize()
  })
})

onBeforeUnmount(() => {
  jobTitleChart?.dispose()
  educationChart?.dispose()
})
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