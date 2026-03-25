package com.student.service;

import com.student.dto.DepartmentStatisticsDto;
import com.student.dto.TimeStatisticsDto;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    
    // 按部门统计报销总额
    List<DepartmentStatisticsDto> getDepartmentStatistics();
    
    // 按时间统计（年/月/日）
    List<TimeStatisticsDto> getTimeStatistics(String period); // year, month, day
    
    // 按状态统计
    Map<String, Object> getStatusStatistics();
    
    // 按项目统计
    List<Map<String, Object>> getProjectStatistics();
    
    // 部门排名
    List<DepartmentStatisticsDto> getDepartmentRanking();
    
    // 审核通过率
    Map<String, Object> getApprovalRate();
    
    // 平均处理时长
    Map<String, Object> getAverageProcessTime();
}