package com.student.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StatisticsDto {
    // 部门统计
    private String departmentName;
    private Integer departmentId;
    private BigDecimal totalAmount;
    private Integer detailCount;
    
    // 时间统计
    private String dateTime;
    private BigDecimal amount;
    private Integer count;
    
    // 状态统计
    private Integer status;
    private String statusName;
    private BigDecimal statusAmount;
    private Integer statusCount;
}
