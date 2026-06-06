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
        <el-form-item prop="code">
          <el-row :gutter="12">
            <el-col :span="16">
              <el-input
                  v-model="loginForm.code"
                  placeholder="验证码"
                  size="large"
                  @keyup.enter="handleLogin"
              />
            </el-col>
            <el-col :span="8">
              <img :src="captchaUrl" @click="getCaptcha" class="captcha-img" alt="验证码" />
            </el-col>
          </el-row>
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
import request from '@/utils/request'
import { setToken } from '@/utils/auth'

const router = useRouter()
const loginFormRef = ref(null)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  code: '',
  uuid: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const loading = ref(false)
const captchaUrl = ref('')

// 获取验证码
const getCaptcha = async () => {
  try {
    const res = await request({
      url: '/captchaImage',
      method: 'get'
    })
    if (res.code === 200) {
      loginForm.uuid = res.uuid
      captchaUrl.value = 'data:image/gif;base64,' + res.img
    }
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

// 登录
const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request({
          url: '/login',
          method: 'post',
          data: {
            username: loginForm.username,
            password: loginForm.password,
            code: loginForm.code,
            uuid: loginForm.uuid
          }
        })
        if (res.code === 200) {
          setToken(res.token)
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          ElMessage.error(res.msg || '登录失败')
          getCaptcha()
        }
      } catch (error) {
        console.error('登录失败', error)
        ElMessage.error('登录失败，请重试')
        getCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  getCaptcha()
})
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
  width: 450px;
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
.captcha-img {
  width: 100%;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style>