<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">机构总数</div><div class="stat-value">78<span class="unit">家</span></div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">综合医院</div><div class="stat-value">42<span class="unit">家</span></div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">专科医院</div><div class="stat-value">18<span class="unit">家</span></div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner"><div class="stat-title">基层机构</div><div class="stat-value">18<span class="unit">家</span></div></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
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

    <!-- 搜索表单 -->
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="机构名称"><el-input v-model="queryParams.orgName" placeholder="请输入机构名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery">搜索</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain @click="handleUpdate" :disabled="single">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain @click="handleDelete" :disabled="multiple">删除</el-button></el-col>
    </el-row>

    <!-- 数据表格 -->
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

    <!-- 对话框 -->
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

// 模拟数据
const institutionList = ref([
  { id: 1, orgCode: '1001', orgName: '市第一人民医院', orgType: '综合医院', orgLevel: '三级甲等', address: '解放路1号', contactPhone: '12345678' },
  { id: 2, orgCode: '1002', orgName: '市第二人民医院', orgType: '综合医院', orgLevel: '三级乙等', address: '建设路2号', contactPhone: '12345679' },
  { id: 3, orgCode: '1003', orgName: '市中医院', orgType: '中医医院', orgLevel: '三级甲等', address: '中医路3号', contactPhone: '12345680' },
  { id: 4, orgCode: '1004', orgName: '市妇幼保健院', orgType: '专科医院', orgLevel: '三级乙等', address: '妇幼路4号', contactPhone: '12345681' },
  { id: 5, orgCode: '1005', orgName: '仁爱医院', orgType: '民营医院', orgLevel: '二级甲等', address: '民营路5号', contactPhone: '12345682' }
])
const total = ref(5)
const loading = ref(false)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, orgName: '' })
const form = ref({})
const formRef = ref(null)
const rules = { orgCode: [{ required: true, message: '请填写机构编码' }], orgName: [{ required: true, message: '请填写机构名称' }] }

// 图表
const typeChartRef = ref(null)
const levelChartRef = ref(null)
let typeChart = null
let levelChart = null

const renderTypeChart = () => {
  if (!typeChartRef.value) return
  if (typeChart) typeChart.dispose()
  typeChart = echarts.init(typeChartRef.value)
  typeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{ type: 'pie', radius: '55%', data: [{ name: '综合医院', value: 42 }, { name: '专科医院', value: 18 }, { name: '社区卫生服务中心', value: 12 }, { name: '乡镇卫生院', value: 6 }], label: { show: true, formatter: '{b}: {d}%' } }]
  })
}

const renderLevelChart = () => {
  if (!levelChartRef.value) return
  if (levelChart) levelChart.dispose()
  levelChart = echarts.init(levelChartRef.value)
  levelChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: ['三级甲等', '三级乙等', '二级甲等', '二级乙等', '一级'] },
    yAxis: { type: 'value', name: '数量' },
    series: [{ type: 'bar', data: [12, 8, 15, 10, 33], itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' } }]
  })
}

const getList = () => { loading.value = false }
const handleQuery = () => { getList() }
const resetQuery = () => { queryParams.value.orgName = ''; getList() }
const handleSelectionChange = (selection) => { ids.value = selection.map(item => item.id); single.value = selection.length !== 1; multiple.value = !selection.length }
const handleAdd = () => { form.value = {}; dialogTitle.value = '添加机构'; dialogVisible.value = true }
const handleUpdate = (row) => { form.value = row || {}; dialogTitle.value = '修改机构'; dialogVisible.value = true }
const submitForm = () => { formRef.value?.validate(valid => { if (valid) { ElMessage.success('操作成功'); dialogVisible.value = false; getList() } }) }
const handleDelete = (row) => { const id = row?.id || ids.value.join(','); ElMessageBox.confirm('确认删除？').then(() => { ElMessage.success('删除成功'); getList() }).catch(() => {}) }

onMounted(() => { renderTypeChart(); renderLevelChart(); window.addEventListener('resize', () => { typeChart?.resize(); levelChart?.resize() }) })
onBeforeUnmount(() => { typeChart?.dispose(); levelChart?.dispose() })
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