<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>健康大数据统计分析决策系统</h2>
        <p>Health Big Data Statistical Analysis Decision System</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              size="large"
              class="login-btn"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <div class="login-tip">
          <span>测试账号：admin / admin123</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { setToken } from '@/utils/auth'

const router = useRouter()
const loginFormRef = ref(null)

console.log('[Login] 组件已加载')

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loading = ref(false)

const handleLogin = () => {
  console.log('[Login] handleLogin 被点击了')
  loginFormRef.value?.validate(async (valid) => {
    console.log('[Login] validate 结果:', valid)
    if (valid) {
      loading.value = true
      try {
        console.log('[Login] 开始调登录接口...')
        const res = await fetch('/dev-api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: loginForm.username,
            password: loginForm.password,
            code: '',
            uuid: ''
          })
        })
        const data = await res.json()
        console.log('[Login] API 返回:', JSON.stringify(data))
        if (data.code === 200 && data.token) {
          console.log('[Login] 写入 token:', data.token.substring(0, 20) + '...')
          setToken(data.token)
          console.log('[Login] token 已写入 cookie')
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          console.warn('[Login] 登录失败, 返回数据:', data)
          ElMessage.error(data.msg || '登录失败')
        }
      } catch (e) {
        console.error('[Login] 异常:', e)
        ElMessage.error(e.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 400px;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
}
.login-title h2 {
  margin: 0 0 8px 0;
  color: #333;
}
.login-title p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.login-form {
  margin-top: 20px;
}
.login-btn {
  width: 100%;
}
.login-tip {
  text-align: center;
  margin-top: 16px;
  color: #999;
  font-size: 12px;
}
</style>



