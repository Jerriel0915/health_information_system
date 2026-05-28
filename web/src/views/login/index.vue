<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>健康大数据统计分析决策系统</h1>
        <p>Health Big Data Statistical Analysis Decision System</p>
      </div>
      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                :prefix-icon="Key"
                size="large"
                style="flex: 1; margin-right: 10px;"
            />
            <div class="captcha-image" @click="fetchCaptcha">
              <img :src="captchaUrl" alt="验证码" />
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p>© 2025 健康大数据应用创新研发中心</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { login, getCaptcha as getCaptchaApi } from '@/api/login'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const captchaUrl = ref('')
const captchaUuid = ref('')

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  captcha: '',
  uuid: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取验证码（改名 fetchCaptcha）
const fetchCaptcha = async () => {
  try {
    const res = await getCaptchaApi()
    if (res.code === 200) {
      captchaUrl.value = 'data:image/jpeg;base64,' + res.img
      captchaUuid.value = res.uuid
      loginForm.uuid = res.uuid
    } else {
      ElMessage.error('获取验证码失败')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请检查网络')
  }
}

// 登录
const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login({
        username: loginForm.username,
        password: loginForm.password,
        code: loginForm.captcha,
        uuid: loginForm.uuid
      })

      if (res.code === 200) {
        localStorage.setItem('token', res.token)
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.msg || '登录失败')
        fetchCaptcha()
        loginForm.captcha = ''
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请重试')
      fetchCaptcha()
      loginForm.captcha = ''
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  fetchCaptcha()
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-box {
  width: 460px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.login-header p {
  font-size: 12px;
  color: #999;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 15px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}

.captcha-image {
  cursor: pointer;
  flex-shrink: 0;
}

.captcha-image img {
  height: 40px;
  width: 120px;
  border-radius: 8px;
  background: #f0f0f0;
}

.login-btn {
  width: 100%;
  border-radius: 8px;
  height: 46px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.login-footer p {
  font-size: 12px;
  color: #999;
}
</style>