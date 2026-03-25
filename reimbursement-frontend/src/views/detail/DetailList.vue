<template>
  <div class="detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <el-button link @click="goBack">← 返回项目列表</el-button>
            <span style="margin-left: 16px; font-size: 18px; font-weight: bold">{{ projectName }} - 报销明细</span>
          </div>
          <div>
            <el-button type="primary" @click="handleAdd">新增明细</el-button>
            <el-button v-if="userRole === 'A' || userRole === 'B'" type="success" @click="batchAudit" :disabled="selectedIds.length === 0">
              批量审批 ({{ selectedIds.length }})
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column v-if="userRole === 'A' || userRole === 'B'" type="selection" width="55" />
        <el-table-column prop="detailNo" label="序号" width="80" />
        <el-table-column prop="department" label="部门" width="180" />
        <el-table-column prop="agent" label="经办人" width="100" />
        <el-table-column prop="content" label="内容" min-width="200" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="200">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <!-- 开始审批/修改审批：只有管理员和部门负责人可见 -->
            <template v-if="userRole === 'A' || userRole === 'B'">
              <el-button v-if="row.status === 0" link @click="handleStartAudit(row)" style="color: #409eff">开始审批</el-button>
              <el-button v-if="row.status !== 0 && userRole === 'A'" link @click="handleStartAudit(row)" style="color: #e6a23c">修改审批</el-button>
            </template>
            <!-- 删除：只有管理员可见 -->
            <el-button v-if="userRole === 'A'" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="summary">
        <el-statistic title="报销总额" :value="summary.totalAmount" :precision="2" />
        <el-statistic title="明细数量" :value="summary.totalCount" />
        <el-statistic title="已通过" :value="summary.approvedCount" />
        <el-statistic title="待审批" :value="summary.pendingCount" />
        <el-statistic title="已驳回" :value="summary.rejectedCount" />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="resetForm">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="formData.department" placeholder="请选择部门" style="width: 100%">
                <el-option label="文案会务与人事管理工作项目组" value="文案会务与人事管理工作项目组" />
                <el-option label="学风建设与文化促进工作项目组" value="学风建设与文化促进工作项目组" />
                <el-option label="学生权益与提案调研工作项目组" value="学生权益与提案调研工作项目组" />
                <el-option label="外联公关与社会融入工作项目组" value="外联公关与社会融入工作项目组" />
                <el-option label="品牌建设与媒体推广工作项目组" value="品牌建设与媒体推广工作项目组" />
                <el-option label="活动创意与素质拓展工作项目组" value="活动创意与素质拓展工作项目组" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经办人" prop="agent">
              <el-input v-model="formData.agent" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="formData.studentNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="内容" prop="content">
              <el-input v-model="formData.content" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="formData.quantity" :min="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发票号码" prop="invoiceNo">
          <el-input v-model="formData.invoiceNo" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select v-model="formData.invoiceType" style="width: 100%">
                <el-option label="电子发票" value="电子发票" />
                <el-option label="普通纸质发票" value="普通纸质发票" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报销类别" prop="reimbursementCategory">
              <el-select v-model="formData.reimbursementCategory" style="width: 100%">
                <el-option label="奖品" value="奖品" />
                <el-option label="舞台音响灯光" value="舞台音响灯光" />
                <el-option label="文件资料印刷费" value="文件资料印刷费" />
                <el-option label="工作简餐" value="工作简餐" />
                <el-option label="服装费" value="服装费" />
                <el-option label="会场费用" value="会场费用" />
                <el-option label="住宿费" value="住宿费" />
                <el-option label="交通费" value="交通费" />
                <el-option label="其他费用" value="其他费用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额类别" prop="amountCategory">
              <el-select v-model="formData.amountCategory" style="width: 100%">
                <el-option label="小于500" value="小于500" />
                <el-option label="超过500，需银行支付凭证" value="超过500，需银行支付凭证" />
                <el-option label="超过1000，作为固定资产" value="超过1000，作为固定资产" />
                <el-option label="超过10000，需走采招" value="超过10000，需走采招" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="汇款方式" prop="paymentMethod">
              <el-select v-model="formData.paymentMethod" style="width: 100%">
                <el-option label="转卡" value="转卡" />
                <el-option label="汇款" value="汇款" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" rows="2" />
        </el-form-item>
        
        <el-divider>凭证附件</el-divider>
        
        <el-form-item label="上传附件">
          <el-upload
            class="upload-demo"
            drag
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{ projectId: projectId, detailId: currentDetailId }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            accept=".jpg,.jpeg,.png,.pdf"
            :limit="10"
            multiple
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .jpg .png .pdf 格式，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="已上传附件" v-if="existingAttachments.length > 0">
          <div v-for="file in existingAttachments" :key="file.id" class="attachment-item">
            <span>{{ file.fileName }}</span>
            <span class="file-size">{{ (file.fileSize / 1024).toFixed(2) }} KB</span>
            <el-button link type="danger" @click="deleteAttachment(file.id)">删除</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看明细详情对话框（包含明细内容和附件） -->
    <el-dialog v-model="viewDetailDialogVisible" title="报销明细详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="部门">{{ currentViewDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ currentViewDetail.agent }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentViewDetail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentViewDetail.content }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentViewDetail.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentViewDetail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentViewDetail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ currentViewDetail.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ currentViewDetail.invoiceType }}</el-descriptions-item>
        <el-descriptions-item label="报销类别">{{ currentViewDetail.reimbursementCategory }}</el-descriptions-item>
        <el-descriptions-item label="金额类别">{{ currentViewDetail.amountCategory }}</el-descriptions-item>
        <el-descriptions-item label="汇款方式">{{ currentViewDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentViewDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>凭证附件</el-divider>
      
      <div v-if="viewAttachmentsList.length === 0" style="text-align: center; color: #999; padding: 20px;">
        暂无附件
      </div>
      <div v-else class="view-attachments">
        <div v-for="file in viewAttachmentsList" :key="file.id" class="view-attachment-item">
          <el-button link type="primary" @click="previewAttachment(file)">
            {{ file.fileName }}
          </el-button>
          <span class="file-size">{{ (file.fileSize / 1024).toFixed(2) }} KB</span>
          <el-button link type="primary" @click="downloadAttachment(file.id)">下载</el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 审批详情对话框 -->
    <el-dialog v-model="auditDialogVisible" title="报销明细审批" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="部门">{{ currentAuditDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ currentAuditDetail.agent }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentAuditDetail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentAuditDetail.content }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentAuditDetail.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentAuditDetail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentAuditDetail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ currentAuditDetail.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ currentAuditDetail.invoiceType }}</el-descriptions-item>
        <el-descriptions-item label="报销类别">{{ currentAuditDetail.reimbursementCategory }}</el-descriptions-item>
        <el-descriptions-item label="金额类别">{{ currentAuditDetail.amountCategory }}</el-descriptions-item>
        <el-descriptions-item label="汇款方式">{{ currentAuditDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAuditDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>凭证附件</el-divider>
      
      <div v-if="auditAttachments.length === 0" style="text-align: center; color: #999; padding: 20px;">
        暂无附件
      </div>
      <div v-else class="audit-attachments">
        <div v-for="file in auditAttachments" :key="file.id" class="audit-attachment-item">
          <el-button link type="primary" @click="previewAttachment(file)">
            {{ file.fileName }}
          </el-button>
          <span class="file-size">{{ (file.fileSize / 1024).toFixed(2) }} KB</span>
          <el-button link type="primary" @click="downloadAttachment(file.id)">下载</el-button>
        </div>
      </div>
      
      <el-divider>审批意见</el-divider>
      
      <el-input
        v-model="auditComment"
        type="textarea"
        :rows="3"
        placeholder="请输入审批意见"
      />
      
      <template #footer>
        <el-button type="success" @click="confirmAudit(1)">通过</el-button>
        <el-button type="danger" @click="confirmAudit(2)">驳回</el-button>
      </template>
    </el-dialog>
    
    <!-- 附件预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="附件预览" width="80%">
      <div v-if="previewFileType === 'pdf'" style="height: 70vh;">
        <iframe :src="previewUrl" width="100%" height="100%" frameborder="0"></iframe>
      </div>
      <div v-else-if="previewFileType === 'image'" style="text-align: center;">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh;" />
      </div>
      <div v-else>
        <p>此文件类型不支持在线预览，请下载后查看。</p>
      </div>
    </el-dialog>
    
    <!-- 批量审批确认对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量审批确认" width="500px">
      <div>
        <p>以下明细将被批量通过：</p>
        <div style="max-height: 300px; overflow: auto; margin-top: 10px;">
          <div v-for="(item, idx) in batchDetails" :key="idx" class="batch-item">
            <span>{{ idx + 1 }}. {{ item.content }} (¥{{ item.totalAmount }})</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchAudit">确定批量通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import type { ReimbursementDetail, Attachment, Project } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const projectId = Number(route.params.projectId)
const loading = ref(false)
const tableData = ref<ReimbursementDetail[]>([])
const projectName = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增明细')
const formRef = ref()
const viewDetailDialogVisible = ref(false)
const currentViewDetail = ref<ReimbursementDetail>({} as ReimbursementDetail)
const viewAttachmentsList = ref<Attachment[]>([])
const auditDialogVisible = ref(false)
const currentAuditDetail = ref<ReimbursementDetail>({} as ReimbursementDetail)
const auditAttachments = ref<Attachment[]>([])
const auditComment = ref('')
const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewFileType = ref('')
const currentDetailId = ref(0)
const existingAttachments = ref<Attachment[]>([])
const selectedIds = ref<number[]>([])
const batchDialogVisible = ref(false)
const batchDetails = ref<ReimbursementDetail[]>([])

const userRole = computed(() => userStore.userInfo?.roleCode)

const summary = reactive({
  totalAmount: 0,
  totalCount: 0,
  approvedCount: 0,
  pendingCount: 0,
  rejectedCount: 0
})

const formData = reactive({
  id: undefined as number | undefined,
  department: '',
  agent: '',
  studentNo: '',
  content: '',
  unitPrice: 0,
  quantity: 1,
  invoiceNo: '',
  invoiceType: '电子发票',
  reimbursementCategory: '奖品',
  amountCategory: '小于500',
  paymentMethod: '转卡',
  remark: ''
})

const formRules = {
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  agent: [{ required: true, message: '请输入经办人', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  invoiceNo: [{ required: true, message: '请输入发票号码', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  reimbursementCategory: [{ required: true, message: '请选择报销类别', trigger: 'change' }],
  amountCategory: [{ required: true, message: '请选择金额类别', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择汇款方式', trigger: 'change' }]
}

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const handleSelectionChange = (val: ReimbursementDetail[]) => {
  selectedIds.value = val.map(v => v.id)
}

const loadData = async () => {
  loading.value = true
  try {
    const project = await request.get(`/project/detail/${projectId}`) as { project: Project }
    const details = await request.get(`/detail/list/${projectId}`) as ReimbursementDetail[]
    const summaryData = await request.get(`/detail/summary/${projectId}`) as typeof summary
    
    projectName.value = project.project.name
    tableData.value = details
    Object.assign(summary, summaryData)
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadAttachments = async (detailId: number) => {
  try {
    const data = await request.get(`/file/list/${detailId}`) as Attachment[]
    existingAttachments.value = data
  } catch (error) {
    existingAttachments.value = []
  }
}

const deleteAttachment = async (id: number) => {
  await ElMessageBox.confirm('确定删除该附件吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/file/delete/${id}`)
    ElMessage.success('删除成功')
    await loadAttachments(currentDetailId.value)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  if (currentDetailId.value) {
    loadAttachments(currentDetailId.value)
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const resetForm = () => {
  formData.id = undefined
  formData.department = ''
  formData.agent = ''
  formData.studentNo = ''
  formData.content = ''
  formData.unitPrice = 0
  formData.quantity = 1
  formData.invoiceNo = ''
  formData.invoiceType = '电子发票'
  formData.reimbursementCategory = '奖品'
  formData.amountCategory = '小于500'
  formData.paymentMethod = '转卡'
  formData.remark = ''
  existingAttachments.value = []
  currentDetailId.value = 0
}

const handleAdd = async () => {
  dialogTitle.value = '新增明细'
  resetForm()
  // 自动填充当前用户信息
  const userInfo = userStore.userInfo
  if (userInfo) {
    formData.agent = userInfo.realName || userInfo.username || ''
    formData.studentNo = userInfo.studentNo || ''
  }
  dialogVisible.value = true
}

const handleEdit = async (row: ReimbursementDetail) => {
  dialogTitle.value = '编辑明细'
  formData.id = row.id
  formData.department = row.department
  formData.agent = row.agent
  formData.studentNo = row.studentNo
  formData.content = row.content
  formData.unitPrice = row.unitPrice
  formData.quantity = row.quantity
  formData.invoiceNo = row.invoiceNo
  formData.invoiceType = row.invoiceType
  formData.reimbursementCategory = row.reimbursementCategory
  formData.amountCategory = row.amountCategory
  formData.paymentMethod = row.paymentMethod
  formData.remark = row.remark || ''
  currentDetailId.value = row.id
  await loadAttachments(row.id)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    if (formData.id) {
      // 编辑模式
      const detail = {
        id: formData.id,
        department: formData.department,
        agent: formData.agent,
        studentNo: formData.studentNo,
        content: formData.content,
        unitPrice: formData.unitPrice,
        quantity: formData.quantity,
        totalAmount: formData.unitPrice * formData.quantity,
        invoiceNo: formData.invoiceNo,
        invoiceType: formData.invoiceType,
        reimbursementCategory: formData.reimbursementCategory,
        amountCategory: formData.amountCategory,
        paymentMethod: formData.paymentMethod,
        remark: formData.remark
      }
      await request.put('/detail/update', detail)
      ElMessage.success('修改成功，请重新提交审批')
    } else {
      // 新增模式
      const details = [formData]
      await request.post('/detail/batch/add', { projectId, details })
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    loadData()  // 刷新列表，状态会变为待审批
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该明细吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/detail/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

const handleView = async (row: ReimbursementDetail) => {
  currentViewDetail.value = row
  try {
    const data = await request.get(`/file/list/${row.id}`) as Attachment[]
    viewAttachmentsList.value = data
  } catch (error) {
    viewAttachmentsList.value = []
  }
  viewDetailDialogVisible.value = true
}

const handleStartAudit = async (row: ReimbursementDetail) => {
  currentAuditDetail.value = row
  auditComment.value = ''
  try {
    const data = await request.get(`/file/list/${row.id}`) as Attachment[]
    auditAttachments.value = data
  } catch (error) {
    auditAttachments.value = []
  }
  auditDialogVisible.value = true
}

const confirmAudit = async (result: number) => {
  let comment = auditComment.value || ''
  
  if (result === 2) {
    try {
      await request.post('/audit/detail', {
        targetId: currentAuditDetail.value.id,
        auditType: 'DETAIL',
        result: result,
        comment: comment
      })
      ElMessage.success('已驳回')
      auditDialogVisible.value = false
      loadData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  } else {
    // 通过审批
    try {
      await request.post('/audit/detail', {
        targetId: currentAuditDetail.value.id,
        auditType: 'DETAIL',
        result: result,
        comment: comment
      })
      ElMessage.success('审批通过')
      auditDialogVisible.value = false
      loadData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const batchAudit = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审批的明细')
    return
  }
  
  batchDetails.value = tableData.value.filter(d => selectedIds.value.includes(d.id))
  batchDialogVisible.value = true
}

const confirmBatchAudit = async () => {
  try {
    await request.post('/audit/detail/batch', {
      auditType: 'DETAIL',
      result: 1,
      targetIds: selectedIds.value
    })
    ElMessage.success('批量审批成功')
    batchDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const downloadAttachment = (id: number) => {
  const token = localStorage.getItem('token')
  const link = document.createElement('a')
  link.href = `/api/file/download/${id}?token=${token}`
  link.download = ''
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const previewAttachment = (file: Attachment) => {
  const token = localStorage.getItem('token')
  const fileName = file.fileName || ''
  const ext = fileName.split('.').pop()?.toLowerCase()
  
  if (ext === 'pdf') {
    previewFileType.value = 'pdf'
    previewUrl.value = `/api/file/download/${file.id}?token=${token}&preview=true`
    previewDialogVisible.value = true
  } else if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext || '')) {
    previewFileType.value = 'image'
    previewUrl.value = `/api/file/download/${file.id}?token=${token}&preview=true`
    previewDialogVisible.value = true
  } else {
    downloadAttachment(file.id)
  }
}

const goBack = () => {
  router.push('/project')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  display: flex;
  gap: 40px;
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.batch-item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.view-attachments {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.view-attachment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.audit-attachments {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.audit-attachment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
</style>