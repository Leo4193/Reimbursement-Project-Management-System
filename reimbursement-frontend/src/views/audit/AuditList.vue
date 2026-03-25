<template>
  <div class="audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批明细</span>
          <el-button type="primary" @click="batchAudit" :disabled="selectedIds.length === 0">
            批量审批 ({{ selectedIds.length }})
          </el-button>
        </div>
      </template>
      
      <el-table :data="pendingList" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="detailNo" label="序号" width="80" />
        <el-table-column prop="projectNo" label="项目编号" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="agent" label="经办人" width="100" />
        <el-table-column prop="content" label="内容" min-width="200" />
        <el-table-column prop="totalAmount" label="金额" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">查看详情</el-button>
            <el-button link type="success" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button link type="danger" @click="handleAudit(row.id, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 明细详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="报销明细详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目名称">{{ currentDetail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="项目编号">{{ currentDetail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ currentDetail.agent }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentDetail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentDetail.content }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentDetail.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentDetail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentDetail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ currentDetail.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ currentDetail.invoiceType }}</el-descriptions-item>
        <el-descriptions-item label="报销类别">{{ currentDetail.reimbursementCategory }}</el-descriptions-item>
        <el-descriptions-item label="金额类别">{{ currentDetail.amountCategory }}</el-descriptions-item>
        <el-descriptions-item label="汇款方式">{{ currentDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleAuditFromDetail(1)">通过</el-button>
        <el-button type="danger" @click="handleAuditFromDetail(2)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface DetailItem {
  id: number
  detailNo: string
  projectNo: string
  projectName: string
  projectId: number
  department: string
  agent: string
  studentNo: string
  content: string
  unitPrice: number
  quantity: number
  totalAmount: number
  invoiceNo: string
  invoiceType: string
  reimbursementCategory: string
  amountCategory: string
  paymentMethod: string
  remark: string
  status: number
}

const loading = ref(false)
const allDetails = ref<DetailItem[]>([])
const selectedIds = ref<number[]>([])
const detailDialogVisible = ref(false)
const currentDetail = ref<DetailItem>({} as DetailItem)

const pendingList = computed(() => allDetails.value.filter(d => d.status === 0))

const loadData = async () => {
  loading.value = true
  try {
    const projects = await request.get('/project/page?current=1&size=100')
    const allDetailsTemp: DetailItem[] = []
    
    for (const project of (projects as any).records) {
      const details = await request.get(`/detail/list/${project.id}`) as DetailItem[]
      details.forEach(d => {
        d.projectNo = project.projectNo
        d.projectName = project.name
        d.projectId = project.id
      })
      allDetailsTemp.push(...details)
    }
    
    allDetails.value = allDetailsTemp
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val: DetailItem[]) => {
  selectedIds.value = val.map(v => v.id)
}

const showDetail = (row: DetailItem) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

const handleAudit = async (id: number, result: number) => {
  const action = result === 1 ? '通过' : '驳回'
  let comment = ''
  
  if (result === 2) {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因：', '驳回确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请填写驳回原因'
      })
      if (!value) return
      comment = value
    } catch {
      return
    }
  } else {
    try {
      await ElMessageBox.confirm(`确定${action}该报销明细吗？`, '审批确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }
  
  try {
    await request.post('/audit/detail', {
      targetId: id,
      auditType: 'DETAIL',
      result,
      comment
    })
    ElMessage.success(`${action}成功`)
    detailDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAuditFromDetail = async (result: number) => {
  await handleAudit(currentDetail.value.id, result)
}

const batchAudit = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审批的明细')
    return
  }
  
  // 显示选中的明细列表
  const selectedDetails = allDetails.value.filter(d => selectedIds.value.includes(d.id))
  let detailListHtml = '<div style="max-height: 300px; overflow: auto;">'
  selectedDetails.forEach((d, idx) => {
    detailListHtml += `<p><strong>${idx + 1}.</strong> ${d.projectName} - ${d.content} (¥${d.totalAmount})</p>`
  })
  detailListHtml += '</div>'
  
  await ElMessageBox.confirm(
    `确定批量通过以下 ${selectedIds.value.length} 条明细吗？<br/>${detailListHtml}`,
    '批量审批确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      await request.post('/audit/detail/batch', {
        auditType: 'DETAIL',
        result: 1,
        targetIds: selectedIds.value
      })
      ElMessage.success('批量审批成功')
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.audit-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>