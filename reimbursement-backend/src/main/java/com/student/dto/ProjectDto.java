package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDto {
    private Long id;
    
    @NotBlank(message = "项目名称不能为空")
    private String name;
    
    @NotNull(message = "活动类型不能为空")
    private Integer typeId;
    
    @NotNull(message = "部门不能为空")
    private List<Integer> departmentIds;  // 改为多选部门ID列表
    
    @NotBlank(message = "项目负责人不能为空")
    private String projectLeader;
    
    @Positive(message = "预算金额必须大于0")
    private BigDecimal budgetAmount;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status = 0;
    
    private String departmentFinanceLeader;
    private String officeFinanceLeader;
    private String activityChairperson;
}