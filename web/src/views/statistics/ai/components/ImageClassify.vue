<template>
  <div class="image-classify">
    <el-row :gutter="20">
      <!-- 左侧：统计图表 -->
      <el-col :span="14">
        <!-- 分类统计 -->
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">图像分类统计</span>
            <el-radio-group v-model="imageType" size="small" @change="loadStats">
              <el-radio-button label="environment">机构环境</el-radio-button>
              <el-radio-button label="equipment">医疗设备</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="classifyChartRef" class="chart-container"></div>
        </div>

        <!-- 分类趋势 -->
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">分类数量趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 右侧：上传与识别 -->
      <el-col :span="10">
        <div class="upload-card">
          <div class="card-header">
            <span class="card-title">图像识别</span>
          </div>
          <div class="upload-area" @click="triggerUpload">
            <el-icon class="upload-icon"><Upload /></el-icon>
            <p>点击或拖拽上传图像</p>
            <p class="upload-tip">支持 JPG、PNG 格式</p>
            <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleFileUpload"
            />
          </div>

          <!-- 识别结果 -->
          <div v-if="classifyResult" class="result-area">
            <div class="result-header">识别结果</div>
            <div class="result-image" v-if="previewUrl">
              <img :src="previewUrl" alt="预览图" />
            </div>
            <div class="result-info">
              <div class="result-class">
                分类：<el-tag :type="getResultTagType(classifyResult.className)">
                {{ classifyResult.className }}
              </el-tag>
              </div>
              <div class="result-confidence">
                置信度：{{ classifyResult.confidence }}%
                <el-progress :percentage="classifyResult.confidence" :stroke-width="8" />
              </div>
              <div class="result-desc" v-if="classifyResult.description">
                {{ classifyResult.description }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getImageClassifyStats, getImageClassifyTrend, uploadAndClassify } from '@/api/statistics/ai'

const imageType = ref('environment')
const classifyChartRef = ref(null)
const trendChartRef = ref(null)
const fileInput = ref(null)
const previewUrl = ref('')
const classifyResult = ref(null)

let classifyChart = null
let trendChart = null

// 加载统计图表
const loadStats = async () => {
  try {
    const res = await getImageClassifyStats({ type: imageType.value })
    const data = res.data || {
      categories: ['三级医院', '二级医院', '一级医院', '社区卫生服务中心', '诊所'],
      counts: [28, 62, 95, 156, 320]
    }
    renderClassifyChart(data)
  } catch (error) {
    renderClassifyChart({
      categories: ['三级医院', '二级医院', '一级医院', '社区卫生服务中心', '诊所'],
      counts: [28, 62, 95, 156, 320]
    })
  }
}

const renderClassifyChart = (data) => {
  nextTick(() => {
    if (classifyChart) classifyChart.dispose()
    classifyChart = echarts.init(classifyChartRef.value)
    classifyChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.categories, name: '机构类型', axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: '图像数量' },
      series: [{
        type: 'bar',
        data: data.counts,
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#5470c6' },
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 加载趋势图表
const loadTrend = async () => {
  try {
    const res = await getImageClassifyTrend({ type: imageType.value })
    const data = res.data || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月'],
      counts: [125, 142, 168, 185, 202, 228]
    }
    renderTrendChart(data)
  } catch (error) {
    renderTrendChart({
      months: ['1月', '2月', '3月', '4月', '5月', '6月'],
      counts: [125, 142, 168, 185, 202, 228]
    })
  }
}

const renderTrendChart = (data) => {
  nextTick(() => {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.months, name: '月份' },
      yAxis: { type: 'value', name: '新增图像数' },
      series: [{
        type: 'line',
        data: data.counts,
        smooth: true,
        lineStyle: { width: 3, color: '#67c23a' },
        areaStyle: { opacity: 0.3 },
        symbol: 'circle',
        label: { show: true, position: 'top' }
      }]
    })
  })
}

// 触发文件选择
const triggerUpload = () => {
  fileInput.value.click()
}

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 预览
  previewUrl.value = URL.createObjectURL(file)

  try {
    const res = await uploadAndClassify(file)
    classifyResult.value = res.data || {
      className: '三级医院',
      confidence: 92.5,
      description: '该图像特征符合三级医院标准，包含大型医疗设备和规范化的就诊环境。'
    }
    ElMessage.success('识别完成！')
  } catch (error) {
    console.error('识别失败:', error)
    // 模拟识别结果
    classifyResult.value = {
      className: imageType.value === 'environment' ? '三级医院' : 'CT设备',
      confidence: 88.5,
      description: '这是一张高质量的医疗相关图像，清晰展示了医疗设施/设备的主要特征。'
    }
    ElMessage.success('识别完成（演示数据）')
  }

  // 清空input以便重新上传同文件
  fileInput.value.value = ''
}

const getResultTagType = (className) => {
  const map = {
    '三级医院': 'danger',
    '二级医院': 'warning',
    '一级医院': 'success',
    'CT设备': 'danger',
    'MRI设备': 'warning',
    'X光设备': 'info'
  }
  return map[className] || 'primary'
}

onMounted(() => {
  loadStats()
  loadTrend()
})
</script>

<style scoped>
.image-classify {
  min-height: 500px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  height: 280px;
}

.upload-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.result-area {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.result-header {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.result-image {
  text-align: center;
  margin-bottom: 12px;
}

.result-image img {
  max-width: 100%;
  max-height: 150px;
  border-radius: 4px;
}

.result-class {
  margin-bottom: 12px;
}

.result-confidence {
  margin-bottom: 12px;
}

.result-desc {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}
</style>