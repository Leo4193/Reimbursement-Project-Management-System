<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <span>个人信息</span>
      </template>
      
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="formData.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="formData.studentNo" />
        </el-form-item>
        
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="formData.departmentId" placeholder="请选择部门">
            <el-option label="文案会务与人事管理工作项目组" :value="1" />
            <el-option label="学风建设与文化促进工作项目组" :value="2" />
            <el-option label="学生权益与提案调研工作项目组" :value="3" />
            <el-option label="外联公关与社会融入工作项目组" :value="4" />
            <el-option label="品牌建设与媒体推广工作项目组" :value="5" />
            <el-option label="活动创意与素质拓展工作项目组" :value="6" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-tag :type="roleType">{{ roleName }}</el-tag>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
          <el-button @click="loadData">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <div class="password-section">
        <h4>修改密码</h4>
        <el-form :model="pwdForm" label-width="120px">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import type { User } from '@/types'

const userStore = useUserStore()
const formRef = ref()

const formData = reactive({
  username: '',
  realName: '',
  studentNo: '',
  departmentId: undefined as number | undefined,
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确' }]
}

const roleName = computed(() => {
  const map: Record<string, string> = { A: '管理员', B: '部门负责人', C: '项目主管' }
  return map[userStore.userInfo?.roleCode || 'C']
})

const roleType = computed(() => {
  const map: Record<string, string> = { A: 'danger', B: 'warning', C: 'info' }
  return map[userStore.userInfo?.roleCode || 'C']
})

const loadData = async () => {
  try {
    const data = await request.get('/user/me') as User
    formData.username = data.username || ''
    formData.realName = data.realName || ''
    formData.studentNo = data.studentNo || ''
    formData.departmentId = data.departmentId
    formData.email = data.email || ''
    formData.phone = data.phone || ''
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const handleSave = async () => {
  await formRef.value?.validate()
  try {
    await request.put('/user/update', {
      realName: formData.realName,
      studentNo: formData.studentNo,
      departmentId: formData.departmentId,
      email: formData.email,
      phone: formData.phone
    })
    ElMessage.success('保存成功')
    if (userStore.userInfo) {
      userStore.userInfo.realName = formData.realName
      userStore.userInfo.studentNo = formData.studentNo
      userStore.userInfo.departmentId = formData.departmentId
      userStore.userInfo.email = formData.email
      userStore.userInfo.phone = formData.phone
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await request.put('/user/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.password-section {
  margin-top: 20px;
}

.password-section h4 {
  margin-bottom: 20px;
}
</style>