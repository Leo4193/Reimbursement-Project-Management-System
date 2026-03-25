<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>报销项目管理系统</h1>
        <p>请登录您的账号</p>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
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
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="login-btn">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
      
      <div class="test-accounts">
        <p>测试账号:</p>
        <p>管理员: admin / 123456</p>
        <p>部门负责人: wangwu / 123456</p>
        <p>项目主管: zhangsan / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

interface LoginResponse {
  token: string
  username: string
  realName: string
  roleCode: 'A' | 'B' | 'C'
  userId: number
}

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const data = await request.post('/auth/login', loginForm) as LoginResponse
    userStore.setToken(data.token)
    userStore.setUserInfo({
      id: data.userId,
      username: data.username,
      realName: data.realName,
      roleCode: data.roleCode
    })
    ElMessage.success('登录成功')
    router.push('/project')
  } catch (error: any) {
    ElMessage.error(error.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
}

.login-footer a {
  color: #409eff;
  text-decoration: none;
}

.test-accounts {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #999;
  text-align: center;
}

.test-accounts p {
  margin: 5px 0;
}
</style>