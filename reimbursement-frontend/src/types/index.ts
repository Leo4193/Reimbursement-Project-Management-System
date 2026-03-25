// 用户类型
export interface User {
  id: number
  username: string
  realName: string
  roleCode: 'A' | 'B' | 'C'
  departmentId?: number
  departmentName?: string
  studentNo?: string
  email?: string
  phone?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  username: string
  realName: string
  roleCode: 'A' | 'B' | 'C'
  userId: number
}

// 项目类型
export interface Project {
  id: number
  projectNo: string
  name: string
  typeId: number
  projectLeader: string
  budgetAmount: number
  applyTime: string
  startTime?: string
  endTime?: string
  status: number
  creatorId: number
  createTime: string
  updateTime?: string
  departmentFinanceLeader?: string
  officeFinanceLeader?: string
  activityChairperson?: string
  departmentIds?: number[]
  departmentNames?: string[]
}

// 报销明细类型
export interface ReimbursementDetail {
  id: number
  projectId: number
  detailNo: string
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
  remark?: string
  status: number
  creatorId?: number
  createTime: string
  updateTime?: string
}

// 附件类型
export interface Attachment {
  id: number
  projectId: number
  detailId?: number
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  uploadBy: number
  uploadTime: string
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 审批记录类型
export interface AuditRecord {
  id: number
  projectId: number
  detailId?: number
  auditorId: number
  auditorName: string
  auditorRole: string
  operationType: string
  beforeStatus: number
  afterStatus: number
  comment?: string
  createTime: string
}

// 部门统计类型
export interface DepartmentStatistics {
  departmentName: string
  departmentId: number
  totalAmount: number
  projectCount: number
  detailCount: number
  avgAmount: number
}

// 时间统计类型
export interface TimeStatistics {
  year: string
  month: string
  day: string
  amount: number
  count: number
}

// 状态统计类型
export interface StatusStatistics {
  pending: { count: number; amount: number }
  approved: { count: number; amount: number }
  rejected: { count: number; amount: number }
}