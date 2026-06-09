<template>
  <div style="padding: 20px">
    <h2>肺炎X光图像分类测试</h2>

    <input type="file" @change="uploadImage" accept="image/*" />

    <div v-if="result" style="margin-top: 20px">
      <h3>分类结果：</h3>
      <p>置信度：{{ (result.confidence * 100).toFixed(2) }}%</p>
      <p>置信度：{{ result.confidence }}</p>
      <p>类别ID：{{ result.class_id }}</p>
    </div>

    <div v-if="loading">正在分析中...</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { getToken } from '@/utils/auth'

const result = ref(null)
const loading = ref(false)

const uploadImage = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  loading.value = true
  result.value = null

  const formData = new FormData()
  formData.append('image', file)

  try {
    const token = getToken()
    const res = await axios.post('http://localhost:8081/ai/classify-bone', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': 'Bearer ' + token
      }
    })
    result.value = res.data
    console.log('返回结果：', res.data)
  } catch (err) {
    console.error('请求失败', err)
    alert('上传失败：' + (err.response?.data?.msg || err.message))
  } finally {
    loading.value = false
  }
}
</script>