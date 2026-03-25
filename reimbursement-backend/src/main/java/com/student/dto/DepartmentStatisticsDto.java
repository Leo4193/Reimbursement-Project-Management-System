package com.student.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepartmentStatisticsDto {
    private String departmentName;
    private Integer departmentId;
    private BigDecimal totalAmount;
    private Integer projectCount;
    private Integer detailCount;
    private BigDecimal avgAmount;
}