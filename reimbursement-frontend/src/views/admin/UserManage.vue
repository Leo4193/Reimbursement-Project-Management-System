<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>人员管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.keyword" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="departmentName" label="部门" width="180" />
        <el-table-column prop="roleCode" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.roleCode)">{{ getRoleText(row.roleCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username" v-if="!formData.id">
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!formData.id">
          <el-input v-model="formData.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="formData.studentNo" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" />
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
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="formData.roleCode" placeholder="请选择角色">
            <el-option label="管理员" value="A" />
            <el-option label="部门负责人" value="B" />
            <el-option label="项目主管" value="C" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface User {
  id: number
  username: string
  realName: string
  studentNo: string
  email: string
  phone: string
  departmentId: number
  departmentName: string
  roleCode: 'A' | 'B' | 'C'
  status: number
}

const loading = ref(false)
const tableData = ref<User[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  realName: '',
  studentNo: '',
  email: '',
  phone: '',
  departmentId: undefined as number | undefined,
  roleCode: 'C' as 'A' | 'B' | 'C'
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确' }]
}

const getRoleText = (role: string) => {
  const map: Record<string, string> = { A: '管理员', B: '部门负责人', C: '项目主管' }
  return map[role] || '未知'
}

const getRoleType = (role: string) => {
  const map: Record<string, string> = { A: 'danger', B: 'warning', C: 'info' }
  return map[role] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request.get('/user/page', {
      params: {
        current: page.current,
        size: page.size,
        keyword: searchForm.keyword
      }
    }) as { records: User[]; total: number }
    tableData.value = data.records
    page.total = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  page.current = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  formData.id = undefined
  formData.username = ''
  formData.password = ''
  formData.realName = ''
  formData.studentNo = ''
  formData.email = ''
  formData.phone = ''
  formData.departmentId = undefined
  formData.roleCode = 'C'
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  dialogTitle.value = '编辑用户'
  formData.id = row.id
  formData.username = row.username
  formData.password = ''
  formData.realName = row.realName
  formData.studentNo = row.studentNo
  formData.email = row.email
  formData.phone = row.phone
  formData.departmentId = row.departmentId
  formData.roleCode = row.roleCode
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    if (formData.id) {
      await request.put('/user/admin/update', {  // 改为 admin/update
        id: formData.id,
        realName: formData.realName,
        studentNo: formData.studentNo,
        email: formData.email,
        phone: formData.phone,
        departmentId: formData.departmentId,
        roleCode: formData.roleCode
      })
      ElMessage.success('修改成功')
    } else {
      await request.post('/user/add', formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/user/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>