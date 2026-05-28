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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginFormRef = ref(null)

// 使用 reactive 代替 ref 解决输入问题
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
  loginFormRef.value?.validate((valid) => {
    if (valid) {
      if (loginForm.username === 'admin' && loginForm.password === 'admin123') {
        loading.value = true
        setTimeout(() => {
          localStorage.setItem('token', 'mock-token-123')
          localStorage.setItem('username', loginForm.username)
          ElMessage.success('登录成功')
          router.push('/dashboard')
          loading.value = false
        }, 500)
      } else {
        ElMessage.error('用户名或密码错误')
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