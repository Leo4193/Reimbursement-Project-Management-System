<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header><span>审核通过率</span></template>
          <div class="stat-value">{{ approvalRate.rate }}%</div>
          <div class="stat-desc">总提交: {{ approvalRate.total }}, 通过: {{ approvalRate.approved }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>平均处理时长</span></template>
          <div class="stat-value">{{ avgProcessTime.avgHours }}小时</div>
          <div class="stat-desc">处理数量: {{ avgProcessTime.count }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>部门排名</span></template>
          <div v-for="(item, idx) in departmentRanking" :key="idx" class="ranking-item">
            <span>{{ idx + 1 }}. {{ item.departmentName }}</span>
            <span>¥{{ item.totalAmount }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header><span>部门报销统计</span></template>
          <BarChart :data="departmentRanking" xField="departmentName" yField="totalAmount" title="各部门报销金额" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>状态分布</span></template>
          <PieChart :data="statusData" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>月度趋势</span></template>
          <LineChart :data="timeData" xField="month" yField="amount" title="月度报销趋势" />
        </el-card>
      </el-col>
    </el-row>
    
    <div style="margin-top: 20px">
      <el-button type="primary" @click="exportExcel">导出Excel</el-button>
      <el-button type="success" @click="exportPdf">导出PDF</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import LineChart from '@/components/charts/LineChart.vue'

interface ApprovalRate {
  total: number
  approved: number
  rate: number
}

interface AvgProcessTime {
  count: number
  avgHours: number
}

interface DepartmentRanking {
  departmentName: string
  totalAmount: number
}

interface StatusDetail {
  count: number
  amount: number
}

interface StatusData {
  pending: StatusDetail
  approved: StatusDetail
  rejected: StatusDetail
}

interface TimeItem {
  month: string
  amount: number
}

const approvalRate = ref<ApprovalRate>({ total: 0, approved: 0, rate: 0 })
const avgProcessTime = ref<AvgProcessTime>({ count: 0, avgHours: 0 })
const departmentRanking = ref<DepartmentRanking[]>([])
const statusData = ref<{ name: string; value: number }[]>([])
const timeData = ref<TimeItem[]>([])

const loadData = async () => {
  try {
    const rate = await request.get('/statistics/approval-rate') as ApprovalRate
    const time = await request.get('/statistics/avg-process-time') as AvgProcessTime
    const ranking = await request.get('/statistics/department/ranking') as DepartmentRanking[]
    const status = await request.get('/statistics/status') as StatusData
    const timeStats = await request.get('/statistics/time?period=month') as TimeItem[]
    
    approvalRate.value = rate
    avgProcessTime.value = time
    departmentRanking.value = ranking
    
    statusData.value = [
      { name: '待审批', value: status.pending.count },
      { name: '已通过', value: status.approved.count },
      { name: '已驳回', value: status.rejected.count }
    ]
    
    timeData.value = timeStats
  } catch (error) {
    console.error('加载失败', error)
  }
}

const exportExcel = () => {
  const token = localStorage.getItem('token')
  window.open(`/api/statistics/export/department?token=${token}`, '_blank')
}

const exportPdf = () => {
  const token = localStorage.getItem('token')
  window.open(`/api/statistics/export/pdf/1?token=${token}`, '_blank')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
}

.stat-desc {
  text-align: center;
  color: #909399;
  margin-top: 8px;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
</style>