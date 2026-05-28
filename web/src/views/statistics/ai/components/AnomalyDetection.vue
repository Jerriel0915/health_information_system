<template>
  <div class="anomaly-detection">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <div class="indicator-card">
          <div class="card-title">异常总数</div>
          <div class="card-value">{{ stats.totalAnomalies || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="card-title">违规收费</div>
          <div class="card-value">{{ stats.costAnomalies || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="card-title">异常就诊</div>
          <div class="card-value">{{ stats.visitAnomalies || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="indicator-card">
          <div class="card-title">已处理</div>
          <div class="card-value">{{ stats.handledCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="异常类型">
          <el-select v-model="queryParams.anomalyType" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="违规收费" value="cost" />
            <el-option label="异常就诊" value="visit" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="已处理" value="handled" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 异常列表 -->
    <div class="table-card">
      <el-table :data="anomalyList" v-loading="loading" stripe>
        <el-table-column prop="anomalyType" label="异常类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.anomalyType === 'cost' ? 'danger' : 'warning'">
              {{ scope.row.anomalyType === 'cost' ? '违规收费' : '异常就诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="异常标题" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="250" />
        <el-table-column prop="amount" label="涉及金额" width="120">
          <template #default="scope">
            {{ formatMoney(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="detectTime" label="检测时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'pending' ? 'danger' : 'success'">
              {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 'pending'"
                link
                type="primary"
                @click="openHandleDialog(scope.row)"
            >
              处理
            </el-button>
            <el-button link type="info" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
      />
    </div>

    <!-- 处理对话框 -->
    <el-dialog title="处理异常" v-model="dialogVisible" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理意见">
          <el-input
              v-model="handleForm.comment"
              type="textarea"
              placeholder="请输入处理意见"
              rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnomalyStats, getAnomalyList, handleAnomaly as handleAnomalyApi } from '@/api/statistics/ai'

const loading = ref(false)
const dialogVisible = ref(false)
const currentRow = ref(null)
const total = ref(0)

const stats = ref({})
const anomalyList = ref([])

const dateRange = ref([])

const queryParams = reactive({
  anomalyType: '',
  status: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10
})

const handleForm = reactive({
  comment: ''
})

const formatMoney = (value) => {
  if (!value) return '--'
  return (value / 10000).toFixed(2) + '万'
}

// 加载统计
const loadStats = async () => {
  try {
    const res = await getAnomalyStats()
    stats.value = res.data || {
      totalAnomalies: 156,
      costAnomalies: 89,
      visitAnomalies: 67,
      handledCount: 45
    }
  } catch (error) {
    stats.value = {
      totalAnomalies: 156,
      costAnomalies: 89,
      visitAnomalies: 67,
      handledCount: 45
    }
  }
}

// 加载异常列表
const loadAnomalyList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    }
    const res = await getAnomalyList(params)
    anomalyList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    anomalyList.value = [
      { id: 1, anomalyType: 'cost', title: '重复收费', description: '患者张三在2025年1月存在重复收取CT检查费用的情况', amount: 850, detectTime: '2025-03-15 10:30:00', status: 'pending' },
      { id: 2, anomalyType: 'cost', title: '超标准收费', description: '患者李四住院期间某检查项目超标准收费', amount: 320, detectTime: '2025-03-14 14:20:00', status: 'pending' },
      { id: 3, anomalyType: 'visit', title: '异常就诊频率', description: '患者王五在短期内频繁就诊于多家医院', amount: 0, detectTime: '2025-03-13 09:15:00', status: 'handled' }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  loadAnomalyList()
}

// 重置
const resetQuery = () => {
  queryParams.anomalyType = ''
  queryParams.status = ''
  dateRange.value = []
  queryParams.pageNum = 1
  loadAnomalyList()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadAnomalyList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadAnomalyList()
}

// 打开处理对话框
const openHandleDialog = (row) => {
  currentRow.value = row
  handleForm.comment = ''
  dialogVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  try {
    await handleAnomalyApi({ id: currentRow.value.id, comment: handleForm.comment })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadStats()
    loadAnomalyList()
  } catch (error) {
    ElMessage.success('处理成功（演示）')
    dialogVisible.value = false
    loadStats()
    loadAnomalyList()
  }
}

// 查看详情
const viewDetail = (row) => {
  ElMessage.info('详情功能开发中')
}

onMounted(() => {
  loadStats()
  loadAnomalyList()
})
</script>

<style scoped>
.anomaly-detection {
  min-height: 500px;
}

.indicator-row {
  margin-bottom: 20px;
}

.indicator-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.indicator-card .card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.indicator-card .card-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.filter-bar {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>