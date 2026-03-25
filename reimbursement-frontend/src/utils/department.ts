// 部门映射：完整名称 -> 简称
export const departmentFullToShort: Record<string, string> = {
  '文案会务与人事管理工作项目组': '办公室',
  '学风建设与文化促进工作项目组': '学风建设部',
  '学生权益与提案调研工作项目组': '权益保障部',
  '外联公关与社会融入工作项目组': '公共关系部',
  '品牌建设与媒体推广工作项目组': '自觉传媒部',
  '活动创意与素质拓展工作项目组': '学生综合素质发展中心'
}

// 部门简称列表
export const departmentShortList = [
  '办公室',
  '学风建设部',
  '权益保障部',
  '公共关系部',
  '自觉传媒部',
  '学生综合素质发展中心'
]

// 部门完整名称列表
export const departmentFullList = [
  '文案会务与人事管理工作项目组',
  '学风建设与文化促进工作项目组',
  '学生权益与提案调研工作项目组',
  '外联公关与社会融入工作项目组',
  '品牌建设与媒体推广工作项目组',
  '活动创意与素质拓展工作项目组'
]

// 部门ID映射
export const departmentIdMap: Record<number, string> = {
  1: '办公室',
  2: '学风建设部',
  3: '权益保障部',
  4: '公共关系部',
  5: '自觉传媒部',
  6: '学生综合素质发展中心'
}

// 获取部门简称
export const getShortDepartmentName = (fullName: string): string => {
  return departmentFullToShort[fullName] || fullName
}

// 获取部门完整名称
export const getFullDepartmentName = (shortName: string): string => {
  const entry = Object.entries(departmentFullToShort).find(([_, v]) => v === shortName)
  return entry ? entry[0] : shortName
}