package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.DepartmentStatisticsDto;
import com.student.dto.TimeStatisticsDto;
import com.student.entity.Project;
import com.student.entity.ProjectDepartment;
import com.student.entity.ReimbursementDetail;
import com.student.entity.SysDict;
import com.student.mapper.ProjectDepartmentMapper;
import com.student.mapper.ReimbursementDetailMapper;
import com.student.mapper.ProjectMapper;
import com.student.mapper.SysDictMapper;
import com.student.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private ReimbursementDetailMapper detailMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private SysDictMapper sysDictMapper;
    
    @Autowired
    private ProjectDepartmentMapper projectDepartmentMapper;

    @Override
    public List<DepartmentStatisticsDto> getDepartmentStatistics() {
        // 获取所有已通过和已驳回的明细
        LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReimbursementDetail::getStatus, Arrays.asList(1, 2));
        List<ReimbursementDetail> details = detailMapper.selectList(wrapper);
        
        // 获取所有项目
        List<Project> projects = projectMapper.selectList(null);
        
        // 获取项目-部门关联关系
        List<ProjectDepartment> projectDepartments = projectDepartmentMapper.selectList(null);
        Map<Long, List<Integer>> projectDeptMap = projectDepartments.stream()
            .collect(Collectors.groupingBy(
                ProjectDepartment::getProjectId,
                Collectors.mapping(ProjectDepartment::getDepartmentId, Collectors.toList())
            ));
        
        // 获取部门字典
        List<SysDict> departments = sysDictMapper.findByType("department");
        Map<Integer, String> deptMap = departments.stream()
            .filter(d -> d.getDictCode().contains("_NICK"))
            .collect(Collectors.toMap(
                d -> Integer.parseInt(d.getId().toString()),
                SysDict::getDictValue,
                (v1, v2) -> v1
            ));
        
        // 按部门分组统计
        Map<Integer, DepartmentStatisticsDto> statsMap = new HashMap<>();
        
        // 初始化所有部门
        for (SysDict dept : departments) {
            if (dept.getDictCode().contains("_NICK")) {
                Integer deptId = Integer.parseInt(dept.getId().toString());
                DepartmentStatisticsDto stats = new DepartmentStatisticsDto();
                stats.setDepartmentId(deptId);
                stats.setDepartmentName(dept.getDictValue());
                stats.setTotalAmount(BigDecimal.ZERO);
                stats.setProjectCount(0);
                stats.setDetailCount(0);
                stats.setAvgAmount(BigDecimal.ZERO);
                statsMap.put(deptId, stats);
            }
        }
        
        // 统计项目数
        for (Project project : projects) {
            List<Integer> deptIds = projectDeptMap.get(project.getId());
            if (deptIds != null) {
                for (Integer deptId : deptIds) {
                    DepartmentStatisticsDto stats = statsMap.get(deptId);
                    if (stats != null) {
                        stats.setProjectCount(stats.getProjectCount() + 1);
                    }
                }
            }
        }
        
        // 统计明细金额
        for (ReimbursementDetail detail : details) {
            // 获取项目关联的部门
            List<Integer> deptIds = projectDeptMap.get(detail.getProjectId());
            if (deptIds != null) {
                for (Integer deptId : deptIds) {
                    DepartmentStatisticsDto stats = statsMap.get(deptId);
                    if (stats != null) {
                        stats.setTotalAmount(stats.getTotalAmount().add(detail.getTotalAmount()));
                        stats.setDetailCount(stats.getDetailCount() + 1);
                    }
                }
            }
        }
        
        // 计算平均金额
        for (DepartmentStatisticsDto stats : statsMap.values()) {
            if (stats.getDetailCount() > 0) {
                stats.setAvgAmount(stats.getTotalAmount()
                    .divide(new BigDecimal(stats.getDetailCount()), 2, RoundingMode.HALF_UP));
            }
        }
        
        // 返回有数据的部门
        return statsMap.values().stream()
            .filter(s -> s.getTotalAmount().compareTo(BigDecimal.ZERO) > 0 || s.getProjectCount() > 0)
            .collect(Collectors.toList());
    }

    @Override
    public List<TimeStatisticsDto> getTimeStatistics(String period) {
        LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReimbursementDetail::getStatus, Arrays.asList(1, 2));
        List<ReimbursementDetail> details = detailMapper.selectList(wrapper);
        
        Map<String, TimeStatisticsDto> timeMap = new LinkedHashMap<>();
        
        for (ReimbursementDetail detail : details) {
            String timeKey;
            String year = detail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy"));
            String month = detail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String day = detail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            switch (period) {
                case "year":
                    timeKey = year;
                    break;
                case "month":
                    timeKey = month;
                    break;
                case "day":
                default:
                    timeKey = day;
                    break;
            }
            
            TimeStatisticsDto stats = timeMap.get(timeKey);
            if (stats == null) {
                stats = new TimeStatisticsDto();
                stats.setYear(year);
                stats.setMonth(month);
                stats.setDay(day);
                stats.setAmount(BigDecimal.ZERO);
                stats.setCount(0);
                timeMap.put(timeKey, stats);
            }
            
            stats.setAmount(stats.getAmount().add(detail.getTotalAmount()));
            stats.setCount(stats.getCount() + 1);
        }
        
        return new ArrayList<>(timeMap.values());
    }

    @Override
    public Map<String, Object> getStatusStatistics() {
        LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
        List<ReimbursementDetail> details = detailMapper.selectList(wrapper);
        
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal approvedAmount = BigDecimal.ZERO;
        BigDecimal rejectedAmount = BigDecimal.ZERO;
        
        for (ReimbursementDetail detail : details) {
            switch (detail.getStatus()) {
                case 0:
                    pendingCount++;
                    pendingAmount = pendingAmount.add(detail.getTotalAmount());
                    break;
                case 1:
                    approvedCount++;
                    approvedAmount = approvedAmount.add(detail.getTotalAmount());
                    break;
                case 2:
                    rejectedCount++;
                    rejectedAmount = rejectedAmount.add(detail.getTotalAmount());
                    break;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("pending", Map.of("count", pendingCount, "amount", pendingAmount));
        result.put("approved", Map.of("count", approvedCount, "amount", approvedAmount));
        result.put("rejected", Map.of("count", rejectedCount, "amount", rejectedAmount));
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getProjectStatistics() {
        List<Project> projects = projectMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Project project : projects) {
            LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReimbursementDetail::getProjectId, project.getId())
                   .in(ReimbursementDetail::getStatus, Arrays.asList(1, 2));
            List<ReimbursementDetail> details = detailMapper.selectList(wrapper);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ReimbursementDetail detail : details) {
                totalAmount = totalAmount.add(detail.getTotalAmount());
            }
            
            Map<String, Object> projectStats = new HashMap<>();
            projectStats.put("projectId", project.getId());
            projectStats.put("projectName", project.getName());
            projectStats.put("projectNo", project.getProjectNo());
            projectStats.put("budgetAmount", project.getBudgetAmount());
            projectStats.put("totalAmount", totalAmount);
            projectStats.put("detailCount", details.size());
            projectStats.put("status", project.getStatus());
            
            result.add(projectStats);
        }
        
        return result;
    }

    @Override
    public List<DepartmentStatisticsDto> getDepartmentRanking() {
        List<DepartmentStatisticsDto> stats = getDepartmentStatistics();
        stats.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return stats;
    }

    @Override
    public Map<String, Object> getApprovalRate() {
        LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
        List<ReimbursementDetail> details = detailMapper.selectList(wrapper);
        
        int total = details.size();
        int approved = 0;
        
        for (ReimbursementDetail detail : details) {
            if (detail.getStatus() == 1) {
                approved++;
            }
        }
        
        double rate = total == 0 ? 0 : (double) approved / total * 100;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("approved", approved);
        result.put("rate", Math.round(rate * 100) / 100.0);
        
        return result;
    }

    @Override
    public Map<String, Object> getAverageProcessTime() {
        List<ReimbursementDetail> details = detailMapper.selectList(null);
        
        long totalTime = 0;
        int count = 0;
        
        for (ReimbursementDetail detail : details) {
            if (detail.getStatus() == 1 || detail.getStatus() == 2) {
                long processTime = System.currentTimeMillis() - 
                    detail.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                totalTime += processTime;
                count++;
            }
        }
        
        double avgHours = count == 0 ? 0 : (totalTime / (double) count) / (1000 * 60 * 60);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("avgHours", Math.round(avgHours * 10) / 10.0);
        
        return result;
    }
}