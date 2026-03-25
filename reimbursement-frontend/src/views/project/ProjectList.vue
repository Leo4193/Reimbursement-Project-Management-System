<template>
  <div class="project-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增项目</el-button>
            <el-button type="success" @click="exportProjectReport">导出项目报表</el-button>
            <el-button type="warning" @click="exportDetailReport">导出明细报表</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待审批" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已驳回" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="projectNo" label="项目编号" width="150" />
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="projectLeader" label="负责人" width="120" />
        <el-table-column prop="budgetAmount" label="预算金额" width="120">
          <template #default="{ row }">¥{{ row.budgetAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleDetail(row.id)">明细</el-button>
            <el-button v-if="row.status === 0 || row.status === 4" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="handleSubmitAudit(row.id)">提交审批</el-button>
            <el-button v-if="userRole === 'A' && row.status === 1" link type="warning" @click="handleAuditProject(row)">项目审批</el-button>
            <el-button v-if="userRole === 'A'" link type="danger" @click="handleDelete(row.id)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="项目负责人" prop="projectLeader">
          <el-input v-model="formData.projectLeader" />
        </el-form-item>
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number v-model="formData.budgetAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="活动类型" prop="typeId">
          <el-select v-model="formData.typeId" placeholder="请选择">
            <el-option label="对外活动" :value="1" />
            <el-option label="校级大型活动" :value="2" />
            <el-option label="会内活动" :value="3" />
            <el-option label="其他活动" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择部门" prop="departmentIds">
          <el-select v-model="formData.departmentIds" multiple placeholder="请选择部门（可多选）" style="width: 100%">
            <el-option label="文案会务与人事管理工作项目组" :value="1" />
            <el-option label="学风建设与文化促进工作项目组" :value="2" />
            <el-option label="学生权益与提案调研工作项目组" :value="3" />
            <el-option label="外联公关与社会融入工作项目组" :value="4" />
            <el-option label="品牌建设与媒体推广工作项目组" :value="5" />
            <el-option label="活动创意与素质拓展工作项目组" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门财务负责人" prop="departmentFinanceLeader">
          <el-input v-model="formData.departmentFinanceLeader" />
        </el-form-item>
        <el-form-item label="办公室财务负责人" prop="officeFinanceLeader">
          <el-input v-model="formData.officeFinanceLeader" />
        </el-form-item>
        <el-form-item label="活动分管主席" prop="activityChairperson">
          <el-input v-model="formData.activityChairperson" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看项目详情对话框（只读） -->
    <el-dialog v-model="viewDialogVisible" title="项目详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ currentViewProject.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ currentViewProject.name }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentViewProject.projectLeader }}</el-descriptions-item>
        <el-descriptions-item label="预算金额">¥{{ currentViewProject.budgetAmount }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentViewProject.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentViewProject.status)">{{ getStatusText(currentViewProject.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动类型" :span="2">{{ getActivityType(currentViewProject.typeId) }}</el-descriptions-item>
        <el-descriptions-item label="部门财务负责人" :span="2">{{ currentViewProject.departmentFinanceLeader || '无' }}</el-descriptions-item>
        <el-descriptions-item label="办公室财务负责人" :span="2">{{ currentViewProject.officeFinanceLeader || '无' }}</el-descriptions-item>
        <el-descriptions-item label="活动分管主席" :span="2">{{ currentViewProject.activityChairperson || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 项目审批对话框 -->
    <el-dialog v-model="auditDialogVisible" title="项目审批" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ currentProject.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ currentProject.name }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentProject.projectLeader }}</el-descriptions-item>
        <el-descriptions-item label="预算金额">¥{{ currentProject.budgetAmount }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentProject.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentProject.status)">{{ getStatusText(currentProject.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动类型" :span="2">{{ getActivityType(currentProject.typeId) }}</el-descriptions-item>
        <el-descriptions-item label="关联部门" :span="2">
          <el-tag v-for="dept in currentViewProject.departmentNames" :key="dept" size="small" style="margin-right: 5px;">
            {{ dept }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="部门财务负责人" :span="2">{{ currentProject.departmentFinanceLeader || '无' }}</el-descriptions-item>
        <el-descriptions-item label="办公室财务负责人" :span="2">{{ currentProject.officeFinanceLeader || '无' }}</el-descriptions-item>
        <el-descriptions-item label="活动分管主席" :span="2">{{ currentProject.activityChairperson || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>审批意见</el-divider>
      
      <el-input
        v-model="auditComment"
        type="textarea"
        :rows="3"
        placeholder="请输入审批意见"
      />
      
      <template #footer>
        <el-button type="success" @click="confirmAuditProject(2)">通过</el-button>
        <el-button type="danger" @click="confirmAuditProject(4)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import type { Project } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Project[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const formRef = ref()
const auditDialogVisible = ref(false)
const currentProject = ref<any>({} as any)
const auditComment = ref('')
const viewDialogVisible = ref(false)
const currentViewProject = ref<any>({})
const userRole = computed(() => userStore.userInfo?.roleCode)

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  projectLeader: '',
  budgetAmount: 0,
  typeId: 2,
  departmentIds: [] as number[],
  departmentFinanceLeader: '',
  officeFinanceLeader: '',
  activityChairperson: ''
})

const formRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectLeader: [{ required: true, message: '请输入项目负责人', trigger: 'blur' }],
  budgetAmount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }],
  departmentIds: [{ required: true, message: '请至少选择一个部门', trigger: 'change', type: 'array', min: 1 }],
  departmentFinanceLeader: [{ required: true, message: '请输入部门财务负责人', trigger: 'blur' }],
  officeFinanceLeader: [{ required: true, message: '请输入办公室财务负责人', trigger: 'blur' }],
  activityChairperson: [{ required: true, message: '请输入活动分管主席', trigger: 'blur' }]
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 1: '待审批', 2: '进行中', 3: '已完成', 4: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: '', 4: 'danger' }
  return map[status] || 'info'
}

const getActivityType = (typeId: number) => {
  const map: Record<number, string> = { 1: '对外活动', 2: '校级大型活动', 3: '会内活动', 4: '其他活动' }
  return map[typeId] || '未知'
}

const canDelete = (row: Project) => {
  if (userRole.value === 'A') return true  // 管理员可以删除所有
  if (userRole.value === 'B') return true  // 部门负责人可以删除本部门的
  if (userRole.value === 'C') return row.creatorId === userStore.userInfo?.id  // 项目主管只能删除自己创建的
  return false
}

const handleView = async (row: Project) => {
  try {
    const detail = await request.get(`/project/detail/${row.id}`) as any
    currentViewProject.value = { ...row, ...detail }
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取项目详情失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request.get('/project/page', {
      params: {
        current: page.current,
        size: page.size,
        keyword: searchForm.keyword,
        status: searchForm.status
      }
    }) as { records: Project[]; total: number }
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
  searchForm.status = undefined
  page.current = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增项目'
  formData.id = undefined
  formData.name = ''
  formData.projectLeader = ''
  formData.budgetAmount = 0
  formData.typeId = 2
  formData.departmentIds = []
  formData.departmentFinanceLeader = ''
  formData.officeFinanceLeader = ''
  formData.activityChairperson = ''
  dialogVisible.value = true
}

const handleEdit = async (row: Project) => {
  dialogTitle.value = '编辑项目'
  formData.id = row.id
  formData.name = row.name
  formData.projectLeader = row.projectLeader
  formData.budgetAmount = row.budgetAmount
  formData.typeId = row.typeId
  // 获取项目的部门ID列表
  try {
    const detail = await request.get(`/project/detail/${row.id}`) as any
    formData.departmentIds = detail.departmentIds || []
  } catch (error) {
    formData.departmentIds = []
  }
  formData.departmentFinanceLeader = (row as any).departmentFinanceLeader || ''
  formData.officeFinanceLeader = (row as any).officeFinanceLeader || ''
  formData.activityChairperson = (row as any).activityChairperson || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    if (formData.id) {
      await request.put('/project/update', formData)
      ElMessage.success('修改成功')
    } else {
      await request.post('/project/add', formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该项目吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/project/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleDetail = (projectId: number) => {
  router.push(`/detail/${projectId}`)
}

const handleSubmitAudit = async (id: number) => {
  await ElMessageBox.confirm('确定提交审批吗？提交后将无法编辑，等待管理员审批。', '提交审批', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
  try {
    await request.put(`/project/status/${id}?status=1`)
    ElMessage.success('提交成功，等待审批')
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleAuditProject = async (row: Project) => {
  try {
    const detail = await request.get(`/project/detail/${row.id}`) as any
    currentProject.value = { ...row, ...detail }
    auditComment.value = ''
    auditDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取项目详情失败')
  }
}

const confirmAuditProject = async (result: number) => {
  if (result === 4 && !auditComment.value) {
    const { value } = await ElMessageBox.prompt('请输入驳回原因（选填）：', '驳回确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '可选填驳回原因'
    })
    if (value === undefined) return
    auditComment.value = value || ''
  }
  
  const action = result === 2 ? '通过' : '驳回'
  
  try {
    await request.post('/audit/project', {
      targetId: currentProject.value.id,
      auditType: 'PROJECT',
      result: result === 2 ? 1 : 2,
      comment: auditComment.value || ''
    })
    ElMessage.success(`项目${action}成功`)
    auditDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const exportProjectReport = () => {
  const token = localStorage.getItem('token')
  const link = document.createElement('a')
  link.href = `/api/statistics/export/project?token=${token}`
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const exportDetailReport = () => {
  const token = localStorage.getItem('token')
  const link = document.createElement('a')
  link.href = `/api/statistics/export/details/1?token=${token}`
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.project-container {
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