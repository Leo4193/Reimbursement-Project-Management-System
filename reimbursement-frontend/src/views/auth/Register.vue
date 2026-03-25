<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1>注册账号</h1>
        <p>请使用同济大学邮箱注册</p>
      </div>
      
      <el-form ref="formRef" :model="formData" :rules="rules" class="register-form">
        <el-form-item prop="studentNo">
          <el-input v-model="formData.studentNo" placeholder="学号" :prefix-icon="User" size="large" />
        </el-form-item>
        
        <el-form-item prop="username">
          <el-input v-model="formData.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="formData.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input v-model="formData.email" placeholder="邮箱（仅限@tongji.edu.cn）" :prefix-icon="Message" size="large" />
        </el-form-item>
        
        <el-form-item prop="realName">
          <el-input v-model="formData.realName" placeholder="真实姓名" size="large" />
        </el-form-item>
        
        <el-form-item prop="departmentId">
          <el-select v-model="formData.departmentId" placeholder="请选择部门" size="large" style="width: 100%">
            <el-option label="文案会务与人事管理工作项目组" :value="1">
              <div>文案会务与人事管理工作项目组</div>
              <div style="font-size: 12px; color: #999;">文案会务与人事管理工作项目组</div>
            </el-option>
            <el-option label="学风建设与文化促进工作项目组" :value="2">
              <div>学风建设与文化促进工作项目组</div>
              <div style="font-size: 12px; color: #999;">学风建设与文化促进工作项目组</div>
            </el-option>
            <el-option label="学生权益与提案调研工作项目组" :value="3">
              <div>学生权益与提案调研工作项目组</div>
              <div style="font-size: 12px; color: #999;">学生权益与提案调研工作项目组</div>
            </el-option>
            <el-option label="外联公关与社会融入工作项目组" :value="4">
              <div>外联公关与社会融入工作项目组</div>
              <div style="font-size: 12px; color: #999;">外联公关与社会融入工作项目组</div>
            </el-option>
            <el-option label="品牌建设与媒体推广工作项目组" :value="5">
              <div>品牌建设与媒体推广工作项目组</div>
              <div style="font-size: 12px; color: #999;">品牌建设与媒体推广工作项目组</div>
            </el-option>
            <el-option label="活动创意与素质拓展工作项目组" :value="6">
              <div>活动创意与素质拓展工作项目组</div>
              <div style="font-size: 12px; color: #999;">活动创意与素质拓展工作项目组</div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item prop="code">
          <div class="code-input">
            <el-input v-model="formData.code" placeholder="验证码" size="large" />
            <el-button :disabled="codeDisabled" @click="sendCode" size="large">
              {{ codeText }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="register-btn">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const codeDisabled = ref(false)
const codeText = ref('获取验证码')
let timer: any = null

const formData = reactive({
  studentNo: '',
  username: '',
  password: '',
  email: '',
  realName: '',
  departmentId: undefined as number | undefined,
  code: ''
})

const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!value.endsWith('@tongji.edu.cn')) {
    callback(new Error('请使用同济大学邮箱（@tongji.edu.cn）注册'))
  } else {
    callback()
  }
}

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const sendCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先填写邮箱')
    return
  }
  if (!formData.email.endsWith('@tongji.edu.cn')) {
    ElMessage.warning('请使用同济大学邮箱')
    return
  }
  
  try {
    await request.post('/auth/send-code', { email: formData.email })
    ElMessage.success('验证码已发送')
    codeDisabled.value = true
    let time = 60
    timer = setInterval(() => {
      time--
      codeText.value = `${time}秒后重试`
      if (time <= 0) {
        clearInterval(timer)
        codeDisabled.value = false
        codeText.value = '获取验证码'
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    await request.post('/auth/register', formData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 500px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 24px;
  color: #333;
}

.register-form {
  margin-top: 20px;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

.register-btn {
  width: 100%;
}

.register-footer {
  margin-top: 20px;
  text-align: center;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
}
</style>